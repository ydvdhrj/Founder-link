package com.founderlink.startupservice.dto;

import com.founderlink.startupservice.entity.StartupStage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class CreateStartupRequest {

	@NotBlank
	private String name;

	private String description;

	private String industry;

	@NotNull
	private StartupStage stage;

	@NotNull
	@PositiveOrZero
	private Double fundingGoal;
}
