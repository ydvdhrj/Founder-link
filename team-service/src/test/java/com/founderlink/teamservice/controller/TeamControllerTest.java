package com.founderlink.teamservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.founderlink.teamservice.entity.InviteStatus;
import com.founderlink.teamservice.entity.TeamMember;
import com.founderlink.teamservice.entity.TeamRole;
import com.founderlink.teamservice.security.JwtAuthenticationFilter;
import com.founderlink.teamservice.service.TeamService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TeamController.class)
@AutoConfigureMockMvc(addFilters = false)
class TeamControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private TeamService teamService;

	@MockitoBean
	private JwtAuthenticationFilter jwtAuthenticationFilter;

	@Test
	void invite_shouldReturn201_whenRequestIsValid() throws Exception {
		String requesterId = "founder-1";
		UUID inviteId = UUID.randomUUID();

		TeamMember saved = TeamMember.builder()
				.id(inviteId)
				.startupId("startup-1")
				.userId("user-22")
				.role(TeamRole.CTO)
				.status(InviteStatus.PENDING)
				.build();

		given(teamService.inviteMember("startup-1", "user-22", TeamRole.CTO, requesterId))
				.willReturn(saved);

		String body = """
				{
				  "startupId": "startup-1",
				  "invitedUserId": "user-22",
				  "role": "CTO"
				}
				""";

		mockMvc.perform(post("/teams/invite")
						.header("X-User-Id", requesterId)
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.startupId").value("startup-1"))
				.andExpect(jsonPath("$.userId").value("user-22"))
				.andExpect(jsonPath("$.role").value("CTO"))
				.andExpect(jsonPath("$.status").value("PENDING"));

		then(teamService).should().inviteMember("startup-1", "user-22", TeamRole.CTO, requesterId);
	}

	@Test
	void invite_shouldReturn400_whenValidationFails() throws Exception {
		String invalidBody = """
				{
				  "startupId": "",
				  "invitedUserId": "",
				  "role": null
				}
				""";

		mockMvc.perform(post("/teams/invite")
						.header("X-User-Id", "founder-1")
						.contentType(MediaType.APPLICATION_JSON)
						.content(invalidBody))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").exists());

		then(teamService).shouldHaveNoInteractions();
	}

	@Test
	void teamByStartup_shouldReturn200_whenMembersFound() throws Exception {
		TeamMember member = TeamMember.builder()
				.id(UUID.randomUUID())
				.startupId("startup-9")
				.userId("user-44")
				.role(TeamRole.CPO)
				.status(InviteStatus.ACCEPTED)
				.build();

		given(teamService.getTeamByStartup(eq("startup-9"))).willReturn(List.of(member));

		mockMvc.perform(get("/teams/startup/{startupId}", "startup-9"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].startupId").value("startup-9"))
				.andExpect(jsonPath("$[0].role").value("CPO"))
				.andExpect(jsonPath("$[0].status").value("ACCEPTED"));

		then(teamService).should().getTeamByStartup("startup-9");
	}
}
