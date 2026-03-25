package com.founderlink.investmentservice;

import com.founderlink.investmentservice.client.StartupServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class InvestmentServiceApplicationTests {

	@MockitoBean
	private RabbitTemplate rabbitTemplate;

	@MockitoBean
	private StartupServiceClient startupServiceClient;

	@Test
	void contextLoads() {
	}
}
