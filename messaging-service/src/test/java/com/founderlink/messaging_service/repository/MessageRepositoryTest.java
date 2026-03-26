package com.founderlink.messaging_service.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.founderlink.messaging_service.document.Message;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;

@DataMongoTest(properties = {
		"spring.config.import=optional:classpath:/"
})
@ActiveProfiles("test")
class MessageRepositoryTest {

	@Autowired
	private MessageRepository messageRepository;

	@AfterEach
	void cleanUp() {
		messageRepository.deleteAll();
	}

	@Test
	void findConversation_shouldReturnMessagesInTimestampOrder_forBothDirections() {
		// given
		messageRepository.deleteAll();
		Message m1 = Message.builder()
				.senderId("user-1")
				.receiverId("user-2")
				.content("first")
				.timestamp(Instant.parse("2026-01-01T10:00:00Z"))
				.build();
		Message m2 = Message.builder()
				.senderId("user-2")
				.receiverId("user-1")
				.content("second")
				.timestamp(Instant.parse("2026-01-01T10:01:00Z"))
				.build();
		Message m3 = Message.builder()
				.senderId("user-3")
				.receiverId("user-1")
				.content("other")
				.timestamp(Instant.parse("2026-01-01T10:02:00Z"))
				.build();
		messageRepository.saveAll(List.of(m2, m3, m1));

		// when
		List<Message> result = messageRepository
				.findBySenderIdAndReceiverIdOrSenderIdAndReceiverIdOrderByTimestampAsc(
						"user-1", "user-2", "user-2", "user-1");

		// then
		assertThat(result).hasSize(2);
		assertThat(result).extracting(Message::getContent).containsExactly("first", "second");
		assertThat(result).allMatch(message ->
				("user-1".equals(message.getSenderId()) && "user-2".equals(message.getReceiverId()))
						|| ("user-2".equals(message.getSenderId()) && "user-1".equals(message.getReceiverId())));
	}
}
