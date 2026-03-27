package com.founderlink.teamservice.controller;

import com.founderlink.teamservice.dto.InviteMemberRequest;
import com.founderlink.teamservice.entity.TeamMember;
import com.founderlink.teamservice.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/teams")
@RequiredArgsConstructor
@Tag(name = "Teams", description = "Team invites (X-User-Id from API Gateway)")
public class TeamController {

	private final TeamService teamService;

	@PostMapping(value = "/invite", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Invite co-founder", description = "Founder-only; invites a co-founder/team member and publishes TEAM_INVITE_SENT to RabbitMQ.")
	@ApiResponse(responseCode = "201", description = "Created",
			content = @Content(schema = @Schema(implementation = TeamMember.class)))
	public ResponseEntity<TeamMember> invite(
			@Valid @RequestBody InviteMemberRequest request,
			@Parameter(description = "Requester founder user id (propagated by API Gateway)", required = true)
			@RequestHeader("X-User-Id") String userId) {
		TeamMember body = teamService.inviteMember(
				request.getStartupId(), request.getInvitedUserId(), request.getRole(), userId);
		return ResponseEntity.status(HttpStatus.CREATED).body(body);
	}

	@PostMapping(value = "/join/{inviteId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Accept invitation")
	public TeamMember join(
			@PathVariable UUID inviteId,
			@Parameter(description = "Invited user id", required = true)
			@RequestHeader("X-User-Id") String userId) {
		return teamService.acceptInvitation(inviteId, userId);
	}

	@GetMapping(value = "/startup/{startupId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "List team members for a startup")
	public List<TeamMember> teamByStartup(@PathVariable String startupId) {
		return teamService.getTeamByStartup(startupId);
	}
}
