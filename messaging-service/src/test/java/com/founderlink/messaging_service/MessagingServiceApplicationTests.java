package com.founderlink.messaging_service;

import com.founderlink.messaging_service.client.UserServiceClient;
import com.founderlink.messaging_service.repository.MessageRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class MessagingServiceApplicationTests {

	@MockitoBean
	private RabbitTemplate rabbitTemplate;

	@MockitoBean
	private UserServiceClient userServiceClient;

	@MockitoBean
	private MessageRepository messageRepository;

	@Test
	void contextLoads() {
	}

}
