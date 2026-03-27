package com.founderlink.investmentservice.controller;

import com.founderlink.investmentservice.dto.CreateInvestmentRequest;
import com.founderlink.investmentservice.entity.Investment;
import com.founderlink.investmentservice.entity.InvestmentStatus;
import com.founderlink.investmentservice.service.InvestmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/investments")
@RequiredArgsConstructor
@Tag(name = "Investments", description = "Investments (investor id from X-User-Id)")
public class InvestmentController {

	private final InvestmentService investmentService;

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Create investment", description = "Verifies startup via Feign, saves PENDING, publishes INVESTMENT_CREATED.")
	@ApiResponse(responseCode = "201", description = "Created",
			content = @Content(schema = @Schema(implementation = Investment.class)))
	public ResponseEntity<Investment> create(
			@Valid @RequestBody CreateInvestmentRequest request,
			@Parameter(description = "Investor user id from API Gateway", required = true)
			@RequestHeader("X-User-Id") String investorId) {
		Investment body = investmentService.createInvestment(
				request.getStartupId(), investorId, request.getAmount());
		return ResponseEntity.status(HttpStatus.CREATED).body(body);
	}

	@GetMapping("/startup/{startupId}")
	@Operation(summary = "List investments for a startup")
	public List<Investment> byStartup(@PathVariable String startupId) {
		return investmentService.getInvestmentsByStartup(startupId);
	}

	@GetMapping("/investor")
	@Operation(summary = "List investments for current investor")
	public List<Investment> byInvestor(
			@Parameter(description = "Investor user id from API Gateway", required = true)
			@RequestHeader("X-User-Id") String investorId) {
		return investmentService.getInvestmentsByInvestor(investorId);
	}

	@PutMapping("/{id}/status")
	@Operation(summary = "Update investment status", description = "Founder-only: approve/reject investment for own startup.")
	public Investment updateStatus(
			@PathVariable String id,
			@RequestParam InvestmentStatus status,
			@Parameter(description = "Founder user id from API Gateway", required = true)
			@RequestHeader("X-User-Id") String founderId) {
		return investmentService.updateInvestmentStatus(id, status, founderId);
	}
}
