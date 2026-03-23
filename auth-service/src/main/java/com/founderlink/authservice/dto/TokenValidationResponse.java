package com.founderlink.authservice.dto;

import java.util.List;

public class TokenValidationResponse {

    private boolean valid;
    private Long userId;
    private String email;
    private List<String> roles;

    public TokenValidationResponse() {
    }

    public TokenValidationResponse(boolean valid, Long userId, String email, List<String> roles) {
        this.valid = valid;
        this.userId = userId;
        this.email = email;
        this.roles = roles;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
