package com.founderlink.messaging_service.service;

import com.founderlink.messaging_service.client.UserServiceClient;
import com.founderlink.messaging_service.config.RabbitMQConfig;
import com.founderlink.messaging_service.document.Message;
import com.founderlink.messaging_service.dto.NewMessageEvent;
import com.founderlink.messaging_service.exception.BusinessValidationException;
import com.founderlink.messaging_service.exception.ResourceNotFoundException;
import com.founderlink.messaging_service.repository.MessageRepository;
import feign.FeignException;
import java.util.List;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class MessageService {

	public static final String ROUTING_KEY_MESSAGE_SENT = "message.sent";

	private final MessageRepository messageRepository;
	private final UserServiceClient userServiceClient;
	private final RabbitTemplate rabbitTemplate;

	@CircuitBreaker(name = "userService", fallbackMethod = "sendMessageFallback")
	public Message sendMessage(String senderId, String receiverId, String content) {
		if (content == null || content.isBlank()) {
			throw new BusinessValidationException("Message content must not be blank");
		}
		try {
			// Receiver existence check (response body is not needed).
			userServiceClient.getUserById(receiverId);
		} catch (FeignException.NotFound e) {
			throw new ResourceNotFoundException("Receiver not found: " + receiverId);
		} catch (FeignException e) {
			throw new IllegalStateException("Could not verify receiver via user-service: " + e.getMessage());
		}

		Message toSave = Message.builder()
				.senderId(senderId)
				.receiverId(receiverId)
				.content(content)
				.isRead(false)
				.build();

		Message saved = messageRepository.save(toSave);

		NewMessageEvent event = NewMessageEvent.builder()
				.eventType(NewMessageEvent.EVENT_TYPE)
				.messageId(saved.getId())
				.senderId(saved.getSenderId())
				.receiverId(saved.getReceiverId())
				.build();

		rabbitTemplate.convertAndSend(
				RabbitMQConfig.FOUNDERLINK_EXCHANGE,
				ROUTING_KEY_MESSAGE_SENT,
				event
		);

		return saved;
	}

	private Message sendMessageFallback(String senderId, String receiverId, String content, Throwable throwable) {
		throw new ResponseStatusException(
				HttpStatus.SERVICE_UNAVAILABLE,
				"user-service is unavailable; message delivery is temporarily blocked");
	}

	public List<Message> getConversation(String userA, String userB) {
		return messageRepository
				.findBySenderIdAndReceiverIdOrSenderIdAndReceiverIdOrderByTimestampAsc(
						userA, userB,
						userB, userA
				);
	}
}

