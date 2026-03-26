package com.founderlink.userservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.founderlink.userservice.dto.ProfileResponse;
import com.founderlink.userservice.security.JwtTokenProvider;
import com.founderlink.userservice.service.ProfileService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ProfileService profileService;

	@MockitoBean
	private JwtTokenProvider jwtTokenProvider;

	@Test
	void getByUserId_shouldReturn200_whenProfileExists() throws Exception {
		ProfileResponse response = new ProfileResponse();
		response.setId(11L);
		response.setUserId(101L);
		response.setName("Alice");
		response.setEmail("alice@founderlink.com");

		given(profileService.getByUserId(101L)).willReturn(response);

		mockMvc.perform(get("/users/{userId}", 101L))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(11))
				.andExpect(jsonPath("$.userId").value(101))
				.andExpect(jsonPath("$.name").value("Alice"))
				.andExpect(jsonPath("$.email").value("alice@founderlink.com"));

		then(profileService).should().getByUserId(101L);
	}

	@Test
	void list_shouldReturn200_whenProfilesFetched() throws Exception {
		ProfileResponse response = new ProfileResponse();
		response.setId(1L);
		response.setUserId(10L);
		response.setName("Founder One");
		response.setEmail("founder1@founderlink.com");

		Page<ProfileResponse> page = new PageImpl<>(List.of(response));
		given(profileService.list(any())).willReturn(page);

		mockMvc.perform(get("/users"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].id").value(1))
				.andExpect(jsonPath("$.content[0].name").value("Founder One"));

		then(profileService).should().list(any());
	}

	@Test
	void create_shouldReturn400_whenValidationFails() throws Exception {
		String invalidBody = """
				{
				  "name": "",
				  "email": "not-an-email"
				}
				""";

		mockMvc.perform(post("/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content(invalidBody))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.title").value("Validation failed"))
				.andExpect(jsonPath("$.errors.name").exists())
				.andExpect(jsonPath("$.errors.email").exists());

		then(profileService).shouldHaveNoInteractions();
	}
}
