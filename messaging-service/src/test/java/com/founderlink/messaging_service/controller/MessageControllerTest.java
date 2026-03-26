package com.founderlink.messaging_service.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.founderlink.messaging_service.document.Message;
import com.founderlink.messaging_service.service.MessageService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MessageController.class)
@AutoConfigureMockMvc(addFilters = false)
class MessageControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private MessageService messageService;

	@Test
	void conversation_shouldReturn200_whenMessagesFound() throws Exception {
		Message message = Message.builder()
				.id("msg-1")
				.senderId("user-1")
				.receiverId("user-2")
				.content("Hello")
				.build();
		given(messageService.getConversation("user-1", "user-2")).willReturn(List.of(message));

		mockMvc.perform(get("/messages/conversation/{otherUserId}", "user-2")
						.header("X-User-Id", "user-1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value("msg-1"))
				.andExpect(jsonPath("$[0].content").value("Hello"));

		then(messageService).should().getConversation("user-1", "user-2");
	}

	@Test
	void send_shouldReturn400_whenValidationFails() throws Exception {
		String invalidBody = """
				{
				  "receiverId": "",
				  "content": ""
				}
				""";

		mockMvc.perform(post("/messages")
						.header("X-User-Id", "user-1")
						.contentType(MediaType.APPLICATION_JSON)
						.content(invalidBody))
				.andExpect(status().isBadRequest());

		then(messageService).shouldHaveNoInteractions();
	}

	@Test
	void send_shouldReturn201_whenRequestIsValid() throws Exception {
		Message saved = Message.builder()
				.id("msg-9")
				.senderId("user-1")
				.receiverId("user-2")
				.content("Ping")
				.build();
		given(messageService.sendMessage("user-1", "user-2", "Ping")).willReturn(saved);

		String validBody = """
				{
				  "receiverId": "user-2",
				  "content": "Ping"
				}
				""";

		mockMvc.perform(post("/messages")
						.header("X-User-Id", "user-1")
						.contentType(MediaType.APPLICATION_JSON)
						.content(validBody))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value("msg-9"))
				.andExpect(jsonPath("$.receiverId").value("user-2"));

		then(messageService).should().sendMessage(eq("user-1"), eq("user-2"), eq("Ping"));
	}
}
