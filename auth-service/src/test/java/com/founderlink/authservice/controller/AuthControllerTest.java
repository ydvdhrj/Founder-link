package com.founderlink.authservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.founderlink.authservice.dto.AuthResponse;
import com.founderlink.authservice.dto.TokenValidationResponse;
import com.founderlink.authservice.security.JwtAuthenticationFilter;
import com.founderlink.authservice.service.AuthService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private AuthService authService;

	@MockitoBean
	private JwtAuthenticationFilter jwtAuthenticationFilter;

	@MockitoBean
	private UserDetailsService userDetailsService;

	@Test
	void register_shouldReturn200_whenRequestIsValid() throws Exception {
		AuthResponse response = new AuthResponse(
				"access-token",
				"refresh-token",
				3600L,
				101L,
				"alice@founderlink.com",
				"Alice",
				List.of("ROLE_FOUNDER"));
		given(authService.register(any())).willReturn(response);

		String body = """
				{
				  "name": "Alice",
				  "email": "alice@founderlink.com",
				  "password": "password123",
				  "role": "ROLE_FOUNDER"
				}
				""";

		mockMvc.perform(post("/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").value("access-token"))
				.andExpect(jsonPath("$.userId").value(101))
				.andExpect(jsonPath("$.email").value("alice@founderlink.com"));

		then(authService).should().register(any());
	}

	@Test
	void login_shouldReturn400_whenValidationFails() throws Exception {
		String invalidBody = """
				{
				  "email": "not-an-email",
				  "password": ""
				}
				""";

		mockMvc.perform(post("/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(invalidBody))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.title").value("Validation failed"))
				.andExpect(jsonPath("$.errors.email").exists())
				.andExpect(jsonPath("$.errors.password").exists());

		then(authService).shouldHaveNoInteractions();
	}

	@Test
	void validate_shouldReturn200_whenRequestIsValid() throws Exception {
		TokenValidationResponse response = new TokenValidationResponse(
				true,
				12L,
				"user@founderlink.com",
				List.of("ROLE_INVESTOR"));
		given(authService.validateAccessToken("access-token")).willReturn(response);

		String body = """
				{
				  "token": "access-token"
				}
				""";

		mockMvc.perform(post("/auth/validate")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.valid").value(true))
				.andExpect(jsonPath("$.userId").value(12))
				.andExpect(jsonPath("$.email").value("user@founderlink.com"));

		then(authService).should().validateAccessToken("access-token");
	}
}
