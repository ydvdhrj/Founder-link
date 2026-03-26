package com.founderlink.messaging_service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.founderlink.messaging_service.client.UserServiceClient;
import com.founderlink.messaging_service.config.RabbitMQConfig;
import com.founderlink.messaging_service.document.Message;
import com.founderlink.messaging_service.dto.NewMessageEvent;
import com.founderlink.messaging_service.exception.BusinessValidationException;
import com.founderlink.messaging_service.exception.ResourceNotFoundException;
import com.founderlink.messaging_service.repository.MessageRepository;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

	@Mock
	private MessageRepository messageRepository;

	@Mock
	private UserServiceClient userServiceClient;

	@Mock
	private RabbitTemplate rabbitTemplate;

	@InjectMocks
	private MessageService messageService;

	@Test
	void sendMessage_shouldSaveAndPublishEvent_whenRequestIsValid() {
		// given
		String senderId = "user-1";
		String receiverId = "user-2";
		String content = "Hello founder!";
		given(userServiceClient.getUserById(receiverId)).willReturn(new Object());
		Message saved = Message.builder()
				.id("msg-1")
				.senderId(senderId)
				.receiverId(receiverId)
				.content(content)
				.isRead(false)
				.build();
		given(messageRepository.save(any(Message.class))).willReturn(saved);

		// when
		Message result = messageService.sendMessage(senderId, receiverId, content);

		// then
		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo("msg-1");
		assertThat(result.getSenderId()).isEqualTo(senderId);
		assertThat(result.getReceiverId()).isEqualTo(receiverId);
		then(messageRepository).should().save(any(Message.class));
		then(rabbitTemplate).should().convertAndSend(
				eq(RabbitMQConfig.FOUNDERLINK_EXCHANGE),
				eq(MessageService.ROUTING_KEY_MESSAGE_SENT),
				any(NewMessageEvent.class));
	}

	@Test
	void sendMessage_shouldThrowNotFound_whenReceiverDoesNotExist() {
		// given
		String receiverId = "missing-user";
		Request request = Request.create(
				Request.HttpMethod.GET,
				"/users/" + receiverId,
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
		FeignException notFound = FeignException.errorStatus("UserServiceClient#getUserById", response);
		given(userServiceClient.getUserById(receiverId)).willThrow(notFound);

		// when / then
		assertThatThrownBy(() -> messageService.sendMessage("user-1", receiverId, "hello"))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessageContaining("Receiver not found");

		then(messageRepository).shouldHaveNoInteractions();
	}

	@Test
	void sendMessage_shouldThrowBusinessValidation_whenContentIsBlank() {
		// given
		String blankContent = "   ";

		// when / then
		assertThatThrownBy(() -> messageService.sendMessage("user-1", "user-2", blankContent))
				.isInstanceOf(BusinessValidationException.class)
				.hasMessageContaining("Message content must not be blank");

		then(userServiceClient).shouldHaveNoInteractions();
		then(messageRepository).shouldHaveNoInteractions();
	}
}
