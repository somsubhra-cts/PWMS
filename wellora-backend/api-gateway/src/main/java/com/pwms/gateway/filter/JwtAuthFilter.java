package com.pwms.gateway.filter;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class JwtAuthFilter extends
        AbstractGatewayFilterFactory<JwtAuthFilter.Config> {

    @Value("${jwt.secret}")
    private String secret;

    public JwtAuthFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {

            String path   = exchange.getRequest().getURI().getPath();
            String method = exchange.getRequest().getMethod().name();
            log.debug("Gateway request: {} {}", method, path);

            if (path.startsWith("/api/notifications/internal/")) {
                log.warn("Blocked internal endpoint access: {}", path);
                return onError(exchange, HttpStatus.FORBIDDEN,
                        "Access denied");
            }

            String authHeader = exchange.getRequest()
                    .getHeaders()
                    .getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Missing or invalid Authorization header for: {}", path);
                return onError(exchange, HttpStatus.UNAUTHORIZED,
                        "Missing Authorization header");
            }

            String token = authHeader.substring(7);

            try {
                Claims claims = Jwts.parser()
                        .verifyWith(getKey())
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

                String  username    = claims.getSubject();
                String  role        = claims.get("role", String.class);
                Integer referenceId = claims.get("referenceId", Integer.class);
                String  refIdStr    = String.valueOf(referenceId);

                log.debug("Authenticated: {} role: {} referenceId: {}",
                        username, role, refIdStr);

                if (!isAuthorized(path, method, role)) {
                    log.warn("Role {} not authorized for {} {}",
                            role, method, path);
                    return onError(exchange, HttpStatus.FORBIDDEN,
                            "Access denied: insufficient role");
                }

                if ("PATIENT".equals(role)
                        && !isOwner(path, method, exchange,
                        referenceId, username)) {
                    log.warn("Patient {} tried to access {}",
                            referenceId, path);
                    return onError(exchange, HttpStatus.FORBIDDEN,
                            "Access denied: not your resource");
                }

                ServerWebExchange mutatedExchange = exchange.mutate()
                        .request(exchange.getRequest().mutate()
                                .header("X-Auth-Username",    username)
                                .header("X-Auth-Role",        role)
                                .header("X-Auth-ReferenceId", refIdStr)
                                .build())
                        .build();

                return chain.filter(mutatedExchange);

            } catch (ExpiredJwtException e) {
                log.warn("Token expired for path: {}", path);
                return onError(exchange, HttpStatus.UNAUTHORIZED,
                        "Token expired");

            } catch (JwtException e) {
                log.warn("Invalid token for path: {}", path);
                return onError(exchange, HttpStatus.UNAUTHORIZED,
                        "Invalid token");
            }
        };
    }

    private boolean isAuthorized(String path, String method, String role) {

        if ("ADMIN".equals(role)) return true;

        if ("PATIENT".equals(role)) {

            if (path.matches("/auth/update/[^/]+")
                    && "PUT".equals(method))                          return true;

            if (path.matches("/api/patients/\\d+")
                    && "GET".equals(method))                          return true;

            if (path.matches("/api/plans/\\d+")
                    && "GET".equals(method))                          return true;
            if (path.matches("/api/plans/\\d+/activities")
                    && "GET".equals(method))                          return true;
            if (path.matches("/api/plans/assignments/patient/\\d+")
                    && "GET".equals(method))                          return true;

            if (path.matches("/api/progress/init")
                    && "POST".equals(method))                         return true;
            if (path.matches("/api/progress/seed")
                    && "POST".equals(method))                         return true;
            if (path.matches("/api/progress/update/\\d+")
                    && "PATCH".equals(method))                        return true;
            if (path.matches("/api/progress/patient/\\d+")
                    && "GET".equals(method))                          return true;
            if (path.matches("/api/progress/patient/\\d+/plan/\\d+")
                    && "GET".equals(method))                          return true;
            if (path.matches("/api/progress/patient/\\d+/date/.+")
                    && "GET".equals(method))                          return true;
            if (path.matches("/api/progress/patient/\\d+/plan/\\d+/date/.+")
                    && "GET".equals(method))                          return true;
            if (path.matches("/api/progress/summary/\\d+/plan/\\d+")
                    && "GET".equals(method))                          return true;

            if (path.matches("/api/notifications/patient/\\d+")
                    && "GET".equals(method))                          return true;
            if (path.matches("/api/notifications/patient/\\d+/unread")
                    && "GET".equals(method))                          return true;
            if (path.matches("/api/notifications/\\d+/read")
                    && "PATCH".equals(method))                        return true;

            if (path.matches("/api/reports/patient/\\d+")
                    && "GET".equals(method))                          return true;
            if (path.matches("/api/reports/patient/\\d+/range")
                    && "GET".equals(method))                          return true;
            if (path.matches("/api/reports/\\d+")
                    && "GET".equals(method))                          return true;
            if (path.matches("/api/reports/download/\\d+")
                    && "GET".equals(method))                          return true;

            return false;
        }

        return false;
    }

    private boolean isOwner(String path, String method,
                            ServerWebExchange exchange,
                            Integer referenceId, String callerUsername) {
        if (referenceId == null) return false;

        String   id = String.valueOf(referenceId);
        String[] s  = path.split("/");

        if (path.matches("/auth/update/[^/]+")
                && "PUT".equals(method)) {
            String pathUsername = s[3];
            boolean owns = pathUsername.equals(callerUsername);
            if (!owns) log.warn(
                    "Patient '{}' tried to update password of '{}'",
                    callerUsername, pathUsername);
            return owns;
        }

        if (path.matches("/api/patients/\\d+"))
            return s[3].equals(id);

        if (path.matches("/api/plans/\\d+")
                || path.matches("/api/plans/\\d+/activities"))
            return true;

        if (path.matches("/api/plans/assignments/patient/\\d+"))
            return s[5].equals(id);

        if ((path.matches("/api/progress/init") || path.matches("/api/progress/seed"))
                && "POST".equals(method)) {
            String queryPatientId = exchange.getRequest()
                    .getQueryParams().getFirst("patientId");
            if (queryPatientId == null) {
                log.warn("Missing patientId query param in {}", path);
                return false;
            }
            boolean owns = queryPatientId.equals(id);
            if (!owns) log.warn(
                    "Patient {} tried to access {} for patientId={}",
                    id, path, queryPatientId);
            return owns;
        }

        if (path.matches("/api/progress/update/\\d+"))
            return s[4].equals(id);

        if (path.matches("/api/progress/patient/\\d+.*"))
            return s[4].equals(id);

        if (path.matches("/api/progress/summary/\\d+/plan/\\d+"))
            return s[4].equals(id);

        if (path.matches("/api/notifications/patient/\\d+.*"))
            return s[4].equals(id);

        if (path.matches("/api/notifications/\\d+/read"))
            return true;

        if (path.matches("/api/reports/patient/\\d+.*"))
            return s[4].equals(id);

        if (path.matches("/api/reports/\\d+")
                || path.matches("/api/reports/download/\\d+"))
            return true;

        log.warn("isOwner: unmatched path → denying: {}", path);
        return false;
    }

    private Mono<Void> onError(ServerWebExchange exchange,
                               HttpStatus status, String message) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders()
                .add("Content-Type", "application/json");
        byte[] bytes = ("{\"error\":\"" + message + "\"}").getBytes();
        var buffer = exchange.getResponse()
                .bufferFactory().wrap(bytes);
        return exchange.getResponse()
                .writeWith(Mono.just(buffer));
    }

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8));
    }

    public static class Config {
        // no config needed
    }
}
