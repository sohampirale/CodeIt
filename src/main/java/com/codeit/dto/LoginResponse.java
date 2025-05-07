// File: LoginResponse.java
package com.codeit.dto;

public class LoginResponse {
    private int status;
    private String message;
    private String token;

    public LoginResponse(int status, String message, String token) {
        this.status = status;
        this.message = message;
        this.token = token;
    }

    // Getters
    public int getStatus() { return status; }
    public String getMessage() { return message; }
    public String getToken() { return token; }
}
