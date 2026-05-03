package com.shieldwatch.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String displayName;
    private String role;
    private String teamId;
}
