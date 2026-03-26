package com.founderlink.teamservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.founderlink.teamservice.client.StartupServiceClient;
import com.founderlink.teamservice.config.RabbitMQConfig;
import com.founderlink.teamservice.dto.StartupDto;
import com.founderlink.teamservice.dto.StartupResponsePayload;
import com.founderlink.teamservice.dto.TeamInviteSentEvent;
import com.founderlink.teamservice.entity.InviteStatus;
import com.founderlink.teamservice.entity.TeamMember;
import com.founderlink.teamservice.entity.TeamRole;
import com.founderlink.teamservice.repository.TeamRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

	@Mock
	private TeamRepository teamRepository;

	@Mock
	private StartupServiceClient startupServiceClient;

	@Mock
	private RabbitTemplate rabbitTemplate;

	@InjectMocks
	private TeamService teamService;

	@Test
	void inviteMember_shouldSaveAndPublishEvent_whenRequesterIsFounder() {
		// given
		String startupId = "startup-1";
		String requesterId = "founder-123";
		String invitedUserId = "user-456";
		TeamRole role = TeamRole.CTO;

		StartupDto startupDto = new StartupDto(startupId, requesterId);
		StartupResponsePayload payload = new StartupResponsePayload(startupDto);
		given(startupServiceClient.getStartupById(startupId)).willReturn(payload);

		UUID inviteId = UUID.randomUUID();
		TeamMember saved = TeamMember.builder()
				.id(inviteId)
				.startupId(startupId)
				.userId(invitedUserId)
				.role(role)
				.status(InviteStatus.PENDING)
				.build();
		given(teamRepository.save(any(TeamMember.class))).willReturn(saved);

		// when
		TeamMember result = teamService.inviteMember(startupId, invitedUserId, role, requesterId);

		// then
		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(inviteId);
		assertThat(result.getStatus()).isEqualTo(InviteStatus.PENDING);
		assertThat(result.getRole()).isEqualTo(TeamRole.CTO);

		ArgumentCaptor<TeamMember> memberCaptor = ArgumentCaptor.forClass(TeamMember.class);
		then(teamRepository).should().save(memberCaptor.capture());
		assertThat(memberCaptor.getValue().getStartupId()).isEqualTo(startupId);
		assertThat(memberCaptor.getValue().getUserId()).isEqualTo(invitedUserId);
		assertThat(memberCaptor.getValue().getStatus()).isEqualTo(InviteStatus.PENDING);

		then(rabbitTemplate).should().convertAndSend(
				eq(RabbitMQConfig.FOUNDERLINK_EXCHANGE),
				eq(TeamService.ROUTING_KEY_TEAM_INVITE),
				any(TeamInviteSentEvent.class));
	}

	@Test
	void acceptInvitation_shouldThrowNotFound_whenInviteDoesNotExistForUser() {
		// given
		UUID inviteId = UUID.randomUUID();
		String userId = "user-404";
		given(teamRepository.findByIdAndUserId(inviteId, userId)).willReturn(Optional.empty());

		// when / then
		assertThatThrownBy(() -> teamService.acceptInvitation(inviteId, userId))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Invitation not found for this user");
	}

	@Test
	void inviteMember_shouldThrowBusinessRuleViolation_whenRequesterIsNotFounder() {
		// given
		String startupId = "startup-2";
		String requesterId = "random-user";
		String actualFounderId = "real-founder";
		String invitedUserId = "user-789";

		StartupDto startupDto = new StartupDto(startupId, actualFounderId);
		StartupResponsePayload payload = new StartupResponsePayload(startupDto);
		given(startupServiceClient.getStartupById(startupId)).willReturn(payload);

		// when / then
		assertThatThrownBy(() -> teamService.inviteMember(startupId, invitedUserId, TeamRole.CPO, requesterId))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Only the startup founder can send invites");

		then(teamRepository).shouldHaveNoInteractions();
		then(rabbitTemplate).shouldHaveNoInteractions();
	}
}
