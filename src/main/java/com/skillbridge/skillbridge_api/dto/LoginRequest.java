package com.skillbridge.skillbridge_api.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
    private String mode; // "LOGIN" or "SIGNUP"
}
