package com.founderlink.startupservice;

import com.founderlink.startupservice.client.UserServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class StartupServiceApplicationTests {

	@MockitoBean
	private RabbitTemplate rabbitTemplate;

	@MockitoBean
	private UserServiceClient userServiceClient;

	@Test
	void contextLoads() {
	}
}
