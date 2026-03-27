package com.founderlink.teamservice.service;

import com.founderlink.teamservice.client.StartupServiceClient;
import com.founderlink.teamservice.config.RabbitMQConfig;
import com.founderlink.teamservice.dto.StartupDto;
import com.founderlink.teamservice.dto.StartupResponsePayload;
import com.founderlink.teamservice.dto.TeamInviteSentEvent;
import com.founderlink.teamservice.entity.InviteStatus;
import com.founderlink.teamservice.entity.TeamMember;
import com.founderlink.teamservice.entity.TeamRole;
import com.founderlink.teamservice.repository.TeamRepository;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class TeamService {

	public static final String ROUTING_KEY_TEAM_INVITE = "team.invite";

	private final TeamRepository teamRepository;
	private final StartupServiceClient startupServiceClient;
	private final RabbitTemplate rabbitTemplate;

	@Transactional
	@CircuitBreaker(name = "startupService", fallbackMethod = "inviteMemberFallback")
	public TeamMember inviteMember(String startupId, String invitedUserId, TeamRole role, String requesterId) {
		StartupDto startup = fetchStartupOrThrow(startupId);
		if (startup.getFounderId() == null || !startup.getFounderId().equals(requesterId)) {
			throw new IllegalArgumentException("Only the startup founder can send invites");
		}
		if (requesterId.equals(invitedUserId)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Founder cannot invite themselves");
		}
		if (startup.getFounderId().equals(invitedUserId)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot invite the startup founder as a co-founder");
		}
		if (teamRepository.existsByStartupIdAndUserId(startupId, invitedUserId)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is already invited or part of this startup team");
		}

		TeamMember member = TeamMember.builder()
				.startupId(startupId)
				.userId(invitedUserId)
				.role(role)
				.status(InviteStatus.PENDING)
				.build();
		TeamMember saved = teamRepository.save(member);

		TeamInviteSentEvent event = TeamInviteSentEvent.builder()
				.eventType(TeamInviteSentEvent.TYPE)
				.inviteId(saved.getId())
				.startupId(saved.getStartupId())
				.invitedUserId(saved.getUserId())
				.role(saved.getRole().name())
				.requesterId(requesterId)
				.build();
		rabbitTemplate.convertAndSend(RabbitMQConfig.FOUNDERLINK_EXCHANGE, ROUTING_KEY_TEAM_INVITE, event);

		return saved;
	}

	private TeamMember inviteMemberFallback(String startupId, String invitedUserId, TeamRole role, String requesterId,
			Throwable throwable) {
		throw new ResponseStatusException(
				HttpStatus.SERVICE_UNAVAILABLE,
				"startup-service is unavailable; team invitation is temporarily blocked");
	}

	@Transactional
	public TeamMember acceptInvitation(UUID inviteId, String userId) {
		TeamMember member = teamRepository.findByIdAndUserId(inviteId, userId)
				.orElseThrow(() -> new IllegalArgumentException("Invitation not found for this user"));
		if (member.getStatus() != InviteStatus.PENDING) {
			throw new IllegalStateException("Invitation is not pending");
		}
		member.setStatus(InviteStatus.ACCEPTED);
		member.setJoinedAt(LocalDateTime.now());
		return teamRepository.save(member);
	}

	@Transactional(readOnly = true)
	public List<TeamMember> getTeamByStartup(String startupId) {
		return teamRepository.findByStartupId(startupId);
	}

	private StartupDto fetchStartupOrThrow(String startupId) {
		try {
			StartupResponsePayload payload = startupServiceClient.getStartupById(startupId);
			if (payload == null || payload.getStartup() == null) {
				throw new IllegalArgumentException("Startup not found: " + startupId);
			}
			return payload.getStartup();
		} catch (FeignException.NotFound e) {
			throw new IllegalArgumentException("Startup not found: " + startupId);
		} catch (FeignException e) {
			throw new IllegalStateException("Could not load startup: " + e.getMessage());
		}
	}
}
