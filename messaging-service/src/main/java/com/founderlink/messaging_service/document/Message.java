package com.founderlink.messaging_service.document;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

	@Id
	private String id;

	private String senderId;

	private String receiverId;

	private String content;

	@Builder.Default
	private Instant timestamp = Instant.now();

	@Builder.Default
	private boolean isRead = false;
}

