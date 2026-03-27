package com.founderlink.startupservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.founderlink.startupservice.dto.StartupResponseDTO;
import com.founderlink.startupservice.entity.Startup;
import com.founderlink.startupservice.entity.StartupStage;
import com.founderlink.startupservice.security.JwtAuthenticationFilter;
import com.founderlink.startupservice.service.StartupService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(StartupController.class)
@AutoConfigureMockMvc(addFilters = false)
class StartupControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private StartupService startupService;

	@MockitoBean
	private JwtAuthenticationFilter jwtAuthenticationFilter;

	@Test
	void getById_shouldReturn200_whenStartupExists() throws Exception {
		UUID startupId = UUID.randomUUID();
		Startup startup = Startup.builder()
				.id(startupId)
				.name("Rocket Labs")
				.description("Space tech")
				.industry("Aerospace")
				.stage(StartupStage.MVP)
				.fundingGoal(300000.0)
				.founderId("101")
				.build();
		StartupResponseDTO response = StartupResponseDTO.builder()
				.startup(startup)
				.founder(null)
				.build();

		given(startupService.getStartupWithFounderDetails(startupId.toString())).willReturn(response);

		mockMvc.perform(get("/startups/{id}", startupId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.startup.id").value(startupId.toString()))
				.andExpect(jsonPath("$.startup.name").value("Rocket Labs"))
				.andExpect(jsonPath("$.startup.stage").value("MVP"));

		then(startupService).should().getStartupWithFounderDetails(startupId.toString());
	}

	@Test
	void create_shouldReturn400_whenValidationFails() throws Exception {
		String invalidBody = """
				{
				  "name": "",
				  "stage": null,
				  "fundingGoal": -1
				}
				""";

		mockMvc.perform(post("/startups")
						.header("X-User-Id", "101")
						.contentType(MediaType.APPLICATION_JSON)
						.content(invalidBody))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.title").value("Validation failed"))
				.andExpect(jsonPath("$.errors.name").exists())
				.andExpect(jsonPath("$.errors.stage").exists())
				.andExpect(jsonPath("$.errors.fundingGoal").exists());

		then(startupService).shouldHaveNoInteractions();
	}

	@Test
	void create_shouldReturn201_whenRequestIsValid() throws Exception {
		UUID startupId = UUID.randomUUID();
		Startup saved = Startup.builder()
				.id(startupId)
				.name("FounderLink AI")
				.description("AI co-pilot")
				.industry("SaaS")
				.stage(StartupStage.EARLY_TRACTION)
				.fundingGoal(250000.0)
				.founderId("founder-1")
				.build();

		given(startupService.createStartup(any(), eq("founder-1"))).willReturn(saved);

		String validBody = """
				{
				  "name": "FounderLink AI",
				  "description": "AI co-pilot",
				  "industry": "SaaS",
				  "stage": "EARLY_TRACTION",
				  "fundingGoal": 250000
				}
				""";

		mockMvc.perform(post("/startups")
						.header("X-User-Id", "founder-1")
						.contentType(MediaType.APPLICATION_JSON)
						.content(validBody))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(startupId.toString()))
				.andExpect(jsonPath("$.name").value("FounderLink AI"))
				.andExpect(jsonPath("$.stage").value("EARLY_TRACTION"));

		then(startupService).should().createStartup(any(), eq("founder-1"));
	}
}
