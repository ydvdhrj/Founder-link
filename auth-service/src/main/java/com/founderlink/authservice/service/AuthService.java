package com.founderlink.authservice.service;

import com.founderlink.authservice.dto.AuthResponse;
import com.founderlink.authservice.dto.LoginRequest;
import com.founderlink.authservice.dto.RefreshTokenRequest;
import com.founderlink.authservice.dto.RegisterRequest;
import com.founderlink.authservice.dto.TokenValidationResponse;
import com.founderlink.authservice.dto.UserResponse;
import com.founderlink.authservice.config.JwtProperties;
import com.founderlink.authservice.entity.Role;
import com.founderlink.authservice.entity.User;
import com.founderlink.authservice.exception.BusinessValidationException;
import com.founderlink.authservice.exception.ResourceNotFoundException;
import com.founderlink.authservice.repo.RoleRepository;
import com.founderlink.authservice.repo.UserRepository;
import com.founderlink.authservice.security.JwtTokenProvider;
import com.founderlink.authservice.security.UserDetailsImpl;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;

    public AuthService(UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtTokenProvider jwtTokenProvider,
            JwtProperties jwtProperties) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtProperties = jwtProperties;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessValidationException("Email already registered");
        }

        Role role = roleRepository.findByName(request.getRole())
                .orElseThrow(() -> new BusinessValidationException("Unknown role"));

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.getRoles().add(role);

        User saved = userRepository.save(user);
        User withRoles = userRepository.findByIdWithRoles(saved.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR));

        return buildAuthResponse(withRoles);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail().toLowerCase(),
                            request.getPassword()));

            UserDetailsImpl principal = (UserDetailsImpl) authentication.getPrincipal();
            User user = userRepository.findByIdWithRoles(principal.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

            return buildAuthResponse(user, authentication);
        } catch (AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials", e);
        }
    }

    @Transactional(readOnly = true)
    public AuthResponse refresh(RefreshTokenRequest request) {
        String token = request.getRefreshToken();
        if (!jwtTokenProvider.validateToken(token) || !jwtTokenProvider.isRefreshToken(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }

        String email = jwtTokenProvider.getEmailFromToken(token).toLowerCase();
        User user = userRepository.findByEmailWithRoles(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        return buildAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public TokenValidationResponse validateAccessToken(String token) {
        if (!jwtTokenProvider.validateToken(token) || !jwtTokenProvider.isAccessToken(token)) {
            return new TokenValidationResponse(false, null, null, null);
        }

        String email = jwtTokenProvider.getEmailFromToken(token).toLowerCase();
        User user = userRepository.findByEmailWithRoles(email).orElse(null);
        if (user == null) {
            return new TokenValidationResponse(false, null, null, null);
        }

        List<String> roles = user.getRoles().stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toList());

        return new TokenValidationResponse(true, user.getId(), user.getEmail(), roles);
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(Long userId) {
        User user = userRepository.findByIdWithRoles(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<String> roles = user.getRoles().stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toList());

        return new UserResponse(user.getId(), user.getName(), user.getEmail(), roles, user.getCreatedAt());
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);
        long expiresSeconds = jwtProperties.getAccessExpirationMs() / 1000L;

        List<String> roles = user.getRoles().stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toList());

        return new AuthResponse(
                accessToken,
                refreshToken,
                expiresSeconds,
                user.getId(),
                user.getEmail(),
                user.getName(),
                roles);
    }

    private AuthResponse buildAuthResponse(User user, Authentication authentication) {
        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);
        long expiresSeconds = jwtProperties.getAccessExpirationMs() / 1000L;

        List<String> roles = user.getRoles().stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toList());

        return new AuthResponse(
                accessToken,
                refreshToken,
                expiresSeconds,
                user.getId(),
                user.getEmail(),
                user.getName(),
                roles);
    }
}
