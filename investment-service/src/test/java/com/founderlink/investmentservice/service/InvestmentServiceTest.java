package com.founderlink.investmentservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.founderlink.investmentservice.client.StartupServiceClient;
import com.founderlink.investmentservice.config.RabbitMQConfig;
import com.founderlink.investmentservice.dto.InvestmentCreatedEvent;
import com.founderlink.investmentservice.entity.Investment;
import com.founderlink.investmentservice.entity.InvestmentStatus;
import com.founderlink.investmentservice.exception.BusinessValidationException;
import com.founderlink.investmentservice.exception.ResourceNotFoundException;
import com.founderlink.investmentservice.repository.InvestmentRepository;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@ExtendWith(MockitoExtension.class)
class InvestmentServiceTest {

	@Mock
	private InvestmentRepository investmentRepository;

	@Mock
	private StartupServiceClient startupServiceClient;

	@Mock
	private RabbitTemplate rabbitTemplate;

	@InjectMocks
	private InvestmentService investmentService;

	@Test
	void createInvestment_shouldSaveAndPublishEvent_whenRequestIsValid() {
		// given
		String startupId = "startup-1";
		String investorId = "investor-1";
		double amount = 10000.0;
		given(startupServiceClient.getStartupById(startupId)).willReturn(Map.of("id", startupId));
		Investment saved = Investment.builder()
				.id(UUID.randomUUID())
				.startupId(startupId)
				.investorId(investorId)
				.amount(amount)
				.status(InvestmentStatus.PENDING)
				.build();
		given(investmentRepository.save(any(Investment.class))).willReturn(saved);

		// when
		Investment result = investmentService.createInvestment(startupId, investorId, amount);

		// then
		assertThat(result).isNotNull();
		assertThat(result.getStartupId()).isEqualTo(startupId);
		assertThat(result.getInvestorId()).isEqualTo(investorId);
		assertThat(result.getAmount()).isEqualTo(amount);
		assertThat(result.getStatus()).isEqualTo(InvestmentStatus.PENDING);
		then(investmentRepository).should().save(any(Investment.class));
		then(rabbitTemplate).should().convertAndSend(
				eq(RabbitMQConfig.FOUNDERLINK_EXCHANGE),
				eq(InvestmentService.ROUTING_KEY_INVESTMENT_CREATED),
				any(InvestmentCreatedEvent.class));
	}

	@Test
	void createInvestment_shouldThrowNotFound_whenStartupMissing() {
		// given
		String startupId = "missing-startup";
		Request request = Request.create(
				Request.HttpMethod.GET,
				"/startups/" + startupId,
				Map.of(),
				null,
				new RequestTemplate());
		Response response = Response.builder()
				.status(404)
				.reason("Not Found")
				.request(request)
				.headers(Map.of())
				.body("not found", StandardCharsets.UTF_8)
				.build();
		FeignException notFound = FeignException.errorStatus("StartupServiceClient#getStartupById", response);
		given(startupServiceClient.getStartupById(startupId)).willThrow(notFound);

		// when / then
		assertThatThrownBy(() -> investmentService.createInvestment(startupId, "investor-1", 5000.0))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessageContaining("Startup not found");

		then(investmentRepository).shouldHaveNoInteractions();
	}

	@Test
	void createInvestment_shouldThrowBusinessValidation_whenAmountInvalid() {
		// given
		Double invalidAmount = 0.0;

		// when / then
		assertThatThrownBy(() -> investmentService.createInvestment("startup-1", "investor-1", invalidAmount))
				.isInstanceOf(BusinessValidationException.class)
				.hasMessageContaining("amount must be greater than zero");

		then(startupServiceClient).shouldHaveNoInteractions();
		then(investmentRepository).shouldHaveNoInteractions();
	}
}
