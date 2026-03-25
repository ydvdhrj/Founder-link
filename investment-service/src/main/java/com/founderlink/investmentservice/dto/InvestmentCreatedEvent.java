package com.founderlink.investmentservice.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvestmentCreatedEvent {

	public static final String TYPE = "INVESTMENT_CREATED";

	private String eventType;
	private UUID investmentId;
	private String startupId;
	private String investorId;
	private Double amount;
}
