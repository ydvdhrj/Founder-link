package com.founderlink.teamservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "team_members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamMember {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "startup_id", nullable = false, length = 64)
	private String startupId;

	@Column(name = "user_id", nullable = false, length = 64)
	private String userId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TeamRole role;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@Builder.Default
	private InviteStatus status = InviteStatus.PENDING;

	@Column(name = "joined_at")
	private LocalDateTime joinedAt;

	@PrePersist
	void onCreate() {
		if (status == null) {
			status = InviteStatus.PENDING;
		}
	}
}
