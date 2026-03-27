package com.founderlink.investmentservice.service;

import com.founderlink.investmentservice.client.StartupServiceClient;
import com.founderlink.investmentservice.config.RabbitMQConfig;
import com.founderlink.investmentservice.dto.InvestmentCreatedEvent;
import com.founderlink.investmentservice.entity.Investment;
import com.founderlink.investmentservice.entity.InvestmentStatus;
import com.founderlink.investmentservice.exception.BusinessValidationException;
import com.founderlink.investmentservice.exception.ResourceNotFoundException;
import com.founderlink.investmentservice.repository.InvestmentRepository;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.Map;
import java.util.List;
import java.util.UUID;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class InvestmentService {

	public static final String ROUTING_KEY_INVESTMENT_CREATED = "investment.created";
	public static final String ROUTING_KEY_INVESTMENT_APPROVED = "investment.status.approved";
	public static final String ROUTING_KEY_INVESTMENT_REJECTED = "investment.status.rejected";

	private final InvestmentRepository investmentRepository;
	private final StartupServiceClient startupServiceClient;
	private final RabbitTemplate rabbitTemplate;

	@Transactional
	public Investment createInvestment(String startupId, String investorId, Double amount) {
		if (amount == null || amount <= 0) {
			throw new BusinessValidationException("Investment amount must be greater than zero");
		}
		verifyStartupWithCircuitBreaker(startupId);

		Investment investment = Investment.builder()
				.startupId(startupId)
				.investorId(investorId)
				.amount(amount)
				.status(InvestmentStatus.PENDING)
				.build();
		Investment saved = investmentRepository.save(investment);

		InvestmentCreatedEvent event = InvestmentCreatedEvent.builder()
				.eventType(InvestmentCreatedEvent.TYPE)
				.investmentId(saved.getId())
				.startupId(saved.getStartupId())
				.investorId(saved.getInvestorId())
				.amount(saved.getAmount())
				.build();
		rabbitTemplate.convertAndSend(RabbitMQConfig.FOUNDERLINK_EXCHANGE, ROUTING_KEY_INVESTMENT_CREATED, event);

		return saved;
	}

	@CircuitBreaker(name = "startupService", fallbackMethod = "verifyStartupFallback")
	private void verifyStartupWithCircuitBreaker(String startupId) {
		try {
			startupServiceClient.getStartupById(startupId);
		} catch (FeignException.NotFound e) {
			throw new ResourceNotFoundException("Startup not found: " + startupId);
		} catch (FeignException e) {
			throw new IllegalStateException("Could not verify startup: " + e.getMessage());
		}
	}

	private void verifyStartupFallback(String startupId, Throwable throwable) {
		throw new ResponseStatusException(
				HttpStatus.SERVICE_UNAVAILABLE,
				"startup-service is unavailable; investment creation is temporarily blocked");
	}

	@Transactional(readOnly = true)
	public List<Investment> getInvestmentsByStartup(String startupId) {
		return investmentRepository.findByStartupId(startupId);
	}

	@Transactional(readOnly = true)
	public List<Investment> getInvestmentsByInvestor(String investorId) {
		return investmentRepository.findByInvestorId(investorId);
	}

	@Transactional
	public Investment updateInvestmentStatus(String investmentId, InvestmentStatus status, String founderId) {
		UUID id;
		try {
			id = UUID.fromString(investmentId);
		} catch (IllegalArgumentException ex) {
			throw new BusinessValidationException("Invalid investment id format: " + investmentId);
		}
		Investment investment = investmentRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Investment not found: " + investmentId));
		String startupFounderId = loadStartupFounderIdWithCircuitBreaker(investment.getStartupId());
		if (startupFounderId == null || !startupFounderId.equals(founderId)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the startup founder can update investment status");
		}
		investment.setStatus(status);
		Investment saved = investmentRepository.save(investment);

		Map<String, Object> event = new HashMap<>();
		event.put("eventType", "INVESTMENT_STATUS_CHANGED");
		event.put("investmentId", saved.getId());
		event.put("startupId", saved.getStartupId());
		event.put("investorId", saved.getInvestorId());
		event.put("amount", saved.getAmount());
		event.put("status", saved.getStatus().name());
		event.put("approvedByFounderId", founderId);

		String routingKey = switch (saved.getStatus()) {
			case APPROVED -> ROUTING_KEY_INVESTMENT_APPROVED;
			case REJECTED -> ROUTING_KEY_INVESTMENT_REJECTED;
			default -> null;
		};
		if (routingKey != null) {
			rabbitTemplate.convertAndSend(RabbitMQConfig.FOUNDERLINK_EXCHANGE, routingKey, event);
		}
		return saved;
	}

	@CircuitBreaker(name = "startupService", fallbackMethod = "loadStartupFounderIdFallback")
	private String loadStartupFounderIdWithCircuitBreaker(String startupId) {
		try {
			Map<String, Object> payload = startupServiceClient.getStartupById(startupId);
			if (payload == null) {
				throw new ResourceNotFoundException("Startup not found: " + startupId);
			}
			Object startupObj = payload.get("startup");
			if (!(startupObj instanceof Map<?, ?> startupMap)) {
				throw new ResourceNotFoundException("Startup not found: " + startupId);
			}
			Object founderId = startupMap.get("founderId");
			return founderId != null ? founderId.toString() : null;
		} catch (FeignException.NotFound e) {
			throw new ResourceNotFoundException("Startup not found: " + startupId);
		} catch (FeignException e) {
			throw new IllegalStateException("Could not verify startup: " + e.getMessage());
		}
	}

	private String loadStartupFounderIdFallback(String startupId, Throwable throwable) {
		throw new ResponseStatusException(
				HttpStatus.SERVICE_UNAVAILABLE,
				"startup-service is unavailable; investment status update is temporarily blocked");
	}
}
