import { Injectable, signal, computed, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthResponse, LoginRequest, RegisterRequest, DecodedToken } from '../models/auth.model';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http   = inject(HttpClient);
  private router = inject(Router);

  private readonly TOKEN_KEY = 'pwms_token';
  private readonly api = environment.apiUrl;

  private _token = signal<string | null>(localStorage.getItem(this.TOKEN_KEY));

  readonly isLoggedIn  = computed(() => !!this._token());
  readonly role        = computed(() => this.decodeToken()?.role ?? null);
  readonly username    = computed(() => this.decodeToken()?.sub ?? null);
  readonly referenceId = computed(() => this.decodeToken()?.referenceId ?? null);

  login(req: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.api}/auth/login`, req).pipe(
      tap(res => this.storeToken(res.token))
    );
  }

  register(req: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.api}/auth/register`, req).pipe(
      tap(res => this.storeToken(res.token))
    );
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    this._token.set(null);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return this._token();
  }

  private storeToken(token: string): void {
    localStorage.setItem(this.TOKEN_KEY, token);
    this._token.set(token);
  }

  private decodeToken(): DecodedToken | null {
    const token = this._token();
    if (!token) return null;
    try {
      return JSON.parse(atob(token.split('.')[1])) as DecodedToken;
    } catch {
      return null;
    }
  }
}
