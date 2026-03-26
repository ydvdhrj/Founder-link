package com.founderlink.messaging_service.controller;

import com.founderlink.messaging_service.document.Message;
import com.founderlink.messaging_service.dto.SendMessageRequest;
import com.founderlink.messaging_service.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
@Tag(name = "Messages", description = "Chat messages (X-User-Id from API Gateway header)")
public class MessageController {

	private final MessageService messageService;

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Send a message", description = "Sends a message from the authenticated sender to the receiver and publishes NEW_MESSAGE to RabbitMQ.")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "Message created",
					content = @Content(schema = @Schema(implementation = Message.class))),
			@ApiResponse(responseCode = "400", description = "Invalid receiver or input")
	})
	public ResponseEntity<Message> send(
			@RequestHeader("X-User-Id") String senderId,
			@Valid @RequestBody SendMessageRequest request
	) {
		Message saved = messageService.sendMessage(senderId, request.getReceiverId(), request.getContent());
		return ResponseEntity.status(HttpStatus.CREATED).body(saved);
	}

	@GetMapping(value = "/conversation/{otherUserId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Get conversation", description = "Returns full ordered chat history between the authenticated sender and the other user.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Conversation loaded",
					content = @Content(array = @ArraySchema(schema = @Schema(implementation = Message.class))))
	})
	public List<Message> conversation(
			@RequestHeader("X-User-Id") String senderId,
			@PathVariable String otherUserId
	) {
		return messageService.getConversation(senderId, otherUserId);
	}
}

