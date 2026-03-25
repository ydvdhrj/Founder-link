package com.founderlink.startupservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
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
@Table(name = "startups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Startup {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false)
	private String name;

	@Lob
	@Column(columnDefinition = "TEXT")
	private String description;

	private String industry;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private StartupStage stage;

	@Column(name = "funding_goal")
	private Double fundingGoal;

	@Column(name = "founder_id", nullable = false, length = 64)
	private String founderId;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@PrePersist
	void onCreate() {
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
	}
}
