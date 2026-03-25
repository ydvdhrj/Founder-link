package com.founderlink.investmentservice.entity;

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
@Table(name = "investments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Investment {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "startup_id", nullable = false, length = 64)
	private String startupId;

	@Column(name = "investor_id", nullable = false, length = 64)
	private String investorId;

	@Column(nullable = false)
	private Double amount;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@Builder.Default
	private InvestmentStatus status = InvestmentStatus.PENDING;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@PrePersist
	void onCreate() {
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
		if (status == null) {
			status = InvestmentStatus.PENDING;
		}
	}
}
