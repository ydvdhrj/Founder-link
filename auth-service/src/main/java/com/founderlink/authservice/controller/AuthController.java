package com.founderlink.authservice.controller;

import com.founderlink.authservice.dto.AuthResponse;
import com.founderlink.authservice.dto.LoginRequest;
import com.founderlink.authservice.dto.RefreshTokenRequest;
import com.founderlink.authservice.dto.RegisterRequest;
import com.founderlink.authservice.dto.TokenValidationResponse;
import com.founderlink.authservice.dto.UserResponse;
import com.founderlink.authservice.dto.ValidateTokenRequest;
import com.founderlink.authservice.security.UserDetailsImpl;
import com.founderlink.authservice.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @PostMapping("/validate")
    public ResponseEntity<TokenValidationResponse> validate(@Valid @RequestBody ValidateTokenRequest request) {
        return ResponseEntity.ok(authService.validateAccessToken(request.getToken()));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal UserDetailsImpl principal) {
        return ResponseEntity.ok(authService.getCurrentUser(principal.getId()));
    }
}
