package com.founderlink.investmentservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CreateInvestmentRequest {

	@NotBlank
	private String startupId;

	@NotNull
	@Positive
	private Double amount;
}
