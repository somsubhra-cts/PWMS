package com.pwms.auth.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponseDTO {
    private String  token;
    private String  username;
    private String  role;
    private Integer referenceId;
    private long    expiresIn;
}