package com.founderlink.notification_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateNotificationTemplateRequest {

	@NotBlank
	private String name;
	private String channel;
	private String subject;
	@NotBlank
	private String body;
}
