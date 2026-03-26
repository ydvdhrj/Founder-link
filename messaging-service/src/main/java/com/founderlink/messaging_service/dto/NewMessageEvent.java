package com.founderlink.messaging_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewMessageEvent {

	public static final String EVENT_TYPE = "NEW_MESSAGE";

	private String eventType;

	private String messageId;
	private String senderId;
	private String receiverId;
}

