package com.founderlink.investmentservice.service;

import com.founderlink.investmentservice.client.StartupServiceClient;
import com.founderlink.investmentservice.config.RabbitMQConfig;
import com.founderlink.investmentservice.dto.InvestmentCreatedEvent;
import com.founderlink.investmentservice.entity.Investment;
import com.founderlink.investmentservice.entity.InvestmentStatus;
import com.founderlink.investmentservice.repository.InvestmentRepository;
import feign.FeignException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InvestmentService {

	public static final String ROUTING_KEY_INVESTMENT_CREATED = "investment.created";

	private final InvestmentRepository investmentRepository;
	private final StartupServiceClient startupServiceClient;
	private final RabbitTemplate rabbitTemplate;

	@Transactional
	public Investment createInvestment(String startupId, String investorId, Double amount) {
		try {
			startupServiceClient.getStartupById(startupId);
		} catch (FeignException.NotFound e) {
			throw new IllegalArgumentException("Startup not found: " + startupId);
		} catch (FeignException e) {
			throw new IllegalStateException("Could not verify startup: " + e.getMessage());
		}

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
//		rabbitTemplate.convertAndSend(RabbitMQConfig.FOUNDERLINK_EXCHANGE, ROUTING_KEY_INVESTMENT_CREATED, event);

		return saved;
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
	public Investment updateInvestmentStatus(String investmentId, InvestmentStatus status) {
		UUID id = UUID.fromString(investmentId);
		Investment investment = investmentRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Investment not found: " + investmentId));
		investment.setStatus(status);
		return investmentRepository.save(investment);
	}
}
