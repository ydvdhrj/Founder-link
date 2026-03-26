package com.founderlink.teamservice.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.founderlink.teamservice.entity.InviteStatus;
import com.founderlink.teamservice.entity.TeamMember;
import com.founderlink.teamservice.entity.TeamRole;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
class TeamRepositoryTest {

	@Autowired
	private TeamRepository teamRepository;

	@Test
	void findByStartupId_shouldReturnMembersForStartup() {
		// given
		TeamMember startupOneMember = TeamMember.builder()
				.startupId("startup-1")
				.userId("user-1")
				.role(TeamRole.CTO)
				.status(InviteStatus.PENDING)
				.build();
		TeamMember startupTwoMember = TeamMember.builder()
				.startupId("startup-2")
				.userId("user-2")
				.role(TeamRole.CPO)
				.status(InviteStatus.ACCEPTED)
				.build();
		teamRepository.save(startupOneMember);
		teamRepository.save(startupTwoMember);

		// when
		List<TeamMember> result = teamRepository.findByStartupId("startup-1");

		// then
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getStartupId()).isEqualTo("startup-1");
		assertThat(result.get(0).getUserId()).isEqualTo("user-1");
		assertThat(result.get(0).getRole()).isEqualTo(TeamRole.CTO);
	}

	@Test
	void findByIdAndUserId_shouldReturnMember_whenMatchingRecordExists() {
		// given
		TeamMember saved = teamRepository.save(TeamMember.builder()
				.startupId("startup-9")
				.userId("user-99")
				.role(TeamRole.MARKETING_HEAD)
				.status(InviteStatus.PENDING)
				.build());

		// when
		Optional<TeamMember> result = teamRepository.findByIdAndUserId(saved.getId(), "user-99");

		// then
		assertThat(result).isPresent();
		assertThat(result.get().getId()).isEqualTo(saved.getId());
		assertThat(result.get().getUserId()).isEqualTo("user-99");
	}

	@Test
	void findByIdAndUserId_shouldReturnEmpty_whenUserIdDoesNotMatch() {
		// given
		TeamMember saved = teamRepository.save(TeamMember.builder()
				.startupId("startup-10")
				.userId("user-actual")
				.role(TeamRole.ENGINEERING_LEAD)
				.status(InviteStatus.PENDING)
				.build());
		UUID inviteId = saved.getId();

		// when
		Optional<TeamMember> result = teamRepository.findByIdAndUserId(inviteId, "user-different");

		// then
		assertThat(result).isEmpty();
	}
}
