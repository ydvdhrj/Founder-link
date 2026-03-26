package com.founderlink.notification_service.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.founderlink.notification_service.model.NotificationTemplate;
import com.founderlink.notification_service.service.NotificationTemplateService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(NotificationTemplateController.class)
@AutoConfigureMockMvc(addFilters = false)
class NotificationTemplateControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private NotificationTemplateService notificationTemplateService;

	@Test
	void getById_shouldReturn200_whenTemplateExists() throws Exception {
		UUID templateId = UUID.randomUUID();
		NotificationTemplate template = NotificationTemplate.builder()
				.id(templateId)
				.name("WELCOME_EMAIL")
				.channel("EMAIL")
				.subject("Welcome")
				.body("Hello user")
				.build();
		given(notificationTemplateService.getTemplateById(templateId)).willReturn(template);

		mockMvc.perform(get("/notifications/templates/{id}", templateId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(templateId.toString()))
				.andExpect(jsonPath("$.name").value("WELCOME_EMAIL"));

		then(notificationTemplateService).should().getTemplateById(templateId);
	}

	@Test
	void create_shouldReturn400_whenValidationFails() throws Exception {
		String invalidBody = """
				{
				  "name": "",
				  "channel": "EMAIL",
				  "subject": "Welcome",
				  "body": ""
				}
				""";

		mockMvc.perform(post("/notifications/templates")
						.contentType(MediaType.APPLICATION_JSON)
						.content(invalidBody))
				.andExpect(status().isBadRequest());

		then(notificationTemplateService).shouldHaveNoInteractions();
	}

	@Test
	void create_shouldReturn201_whenRequestIsValid() throws Exception {
		UUID templateId = UUID.randomUUID();
		NotificationTemplate template = NotificationTemplate.builder()
				.id(templateId)
				.name("PITCH_APPROVED")
				.channel("EMAIL")
				.subject("Pitch Approved")
				.body("Congrats!")
				.build();
		given(notificationTemplateService.createTemplate(any())).willReturn(template);

		String validBody = """
				{
				  "name": "PITCH_APPROVED",
				  "channel": "EMAIL",
				  "subject": "Pitch Approved",
				  "body": "Congrats!"
				}
				""";

		mockMvc.perform(post("/notifications/templates")
						.contentType(MediaType.APPLICATION_JSON)
						.content(validBody))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(templateId.toString()))
				.andExpect(jsonPath("$.name").value("PITCH_APPROVED"));

		then(notificationTemplateService).should().createTemplate(any());
	}
}
