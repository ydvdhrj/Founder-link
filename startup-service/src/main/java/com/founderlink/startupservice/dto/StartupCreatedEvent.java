package com.founderlink.startupservice.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StartupCreatedEvent {

	public static final String TYPE = "STARTUP_CREATED";

	private String eventType;
	private UUID startupId;
	private String founderId;
	private String industry;
	private Double fundingGoal;
}
