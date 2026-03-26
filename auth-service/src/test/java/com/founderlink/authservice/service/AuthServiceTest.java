package com.founderlink.authservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.founderlink.authservice.config.JwtProperties;
import com.founderlink.authservice.dto.AuthResponse;
import com.founderlink.authservice.dto.RegisterRequest;
import com.founderlink.authservice.entity.Role;
import com.founderlink.authservice.entity.RoleName;
import com.founderlink.authservice.entity.User;
import com.founderlink.authservice.exception.BusinessValidationException;
import com.founderlink.authservice.exception.ResourceNotFoundException;
import com.founderlink.authservice.repo.RoleRepository;
import com.founderlink.authservice.repo.UserRepository;
import com.founderlink.authservice.security.JwtTokenProvider;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private RoleRepository roleRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private AuthenticationManager authenticationManager;

	@Mock
	private JwtTokenProvider jwtTokenProvider;

	@Mock
	private JwtProperties jwtProperties;

	@InjectMocks
	private AuthService authService;

	@Test
	void register_shouldReturnTokensAndUserInfo_whenRequestIsValid() {
		// given
		RegisterRequest request = new RegisterRequest();
		request.setName("Alice");
		request.setEmail("ALICE@FounderLink.com");
		request.setPassword("password123");
		request.setRole(RoleName.ROLE_FOUNDER);

		Role founderRole = new Role(RoleName.ROLE_FOUNDER);
		User saved = new User();
		saved.setId(100L);
		saved.setName("Alice");
		saved.setEmail("alice@founderlink.com");
		saved.setPassword("encoded-pass");
		saved.getRoles().add(founderRole);

		User withRoles = new User();
		withRoles.setId(100L);
		withRoles.setName("Alice");
		withRoles.setEmail("alice@founderlink.com");
		withRoles.setPassword("encoded-pass");
		withRoles.getRoles().add(founderRole);

		given(userRepository.existsByEmail("ALICE@FounderLink.com")).willReturn(false);
		given(roleRepository.findByName(RoleName.ROLE_FOUNDER)).willReturn(Optional.of(founderRole));
		given(passwordEncoder.encode("password123")).willReturn("encoded-pass");
		given(userRepository.save(org.mockito.ArgumentMatchers.any(User.class))).willReturn(saved);
		given(userRepository.findByIdWithRoles(100L)).willReturn(Optional.of(withRoles));
		given(jwtTokenProvider.generateAccessToken(withRoles)).willReturn("access-token");
		given(jwtTokenProvider.generateRefreshToken(withRoles)).willReturn("refresh-token");
		given(jwtProperties.getAccessExpirationMs()).willReturn(3_600_000L);

		// when
		AuthResponse response = authService.register(request);

		// then
		assertThat(response).isNotNull();
		assertThat(response.getAccessToken()).isEqualTo("access-token");
		assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
		assertThat(response.getUserId()).isEqualTo(100L);
		assertThat(response.getEmail()).isEqualTo("alice@founderlink.com");
		assertThat(response.getRoles()).containsExactly("ROLE_FOUNDER");
		then(userRepository).should().save(org.mockito.ArgumentMatchers.any(User.class));
	}

	@Test
	void getCurrentUser_shouldThrowResourceNotFound_whenUserDoesNotExist() {
		// given
		given(userRepository.findByIdWithRoles(999L)).willReturn(Optional.empty());

		// when / then
		assertThatThrownBy(() -> authService.getCurrentUser(999L))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessageContaining("User not found");
	}

	@Test
	void register_shouldThrowBusinessValidation_whenEmailAlreadyRegistered() {
		// given
		RegisterRequest request = new RegisterRequest();
		request.setName("Bob");
		request.setEmail("bob@founderlink.com");
		request.setPassword("password123");
		request.setRole(RoleName.ROLE_INVESTOR);
		given(userRepository.existsByEmail("bob@founderlink.com")).willReturn(true);

		// when / then
		assertThatThrownBy(() -> authService.register(request))
				.isInstanceOf(BusinessValidationException.class)
				.hasMessageContaining("Email already registered");

		then(roleRepository).shouldHaveNoInteractions();
	}
}
