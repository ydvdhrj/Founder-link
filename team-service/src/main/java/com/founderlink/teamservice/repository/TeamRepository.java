package com.founderlink.teamservice.repository;

import com.founderlink.teamservice.entity.TeamMember;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<TeamMember, UUID> {

	List<TeamMember> findByStartupId(String startupId);

	Optional<TeamMember> findByIdAndUserId(UUID id, String userId);
}
