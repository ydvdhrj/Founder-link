package com.founderlink.userservice.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class JwtPrincipal {

    private final Long userId;
    private final String email;
    private final Collection<GrantedAuthority> authorities;

    public JwtPrincipal(Long userId, String email, List<String> roles) {
        this.userId = userId;
        this.email = email;
        this.authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public Collection<GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public boolean hasRole(String role) {
        return authorities.stream().anyMatch(a -> a.getAuthority().equals(role));
    }
}
