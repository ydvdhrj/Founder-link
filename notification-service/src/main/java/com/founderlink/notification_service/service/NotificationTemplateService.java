package com.founderlink.notification_service.service;

import com.founderlink.notification_service.dto.CreateNotificationTemplateRequest;
import com.founderlink.notification_service.exception.BusinessValidationException;
import com.founderlink.notification_service.exception.ResourceNotFoundException;
import com.founderlink.notification_service.model.NotificationTemplate;
import com.founderlink.notification_service.repository.NotificationTemplateRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationTemplateService {

	private final NotificationTemplateRepository notificationTemplateRepository;

	public NotificationTemplate createTemplate(CreateNotificationTemplateRequest request) {
		if (request == null || isBlank(request.getName())) {
			throw new BusinessValidationException("Template name is required");
		}
		if (isBlank(request.getBody())) {
			throw new BusinessValidationException("Template body is required");
		}
		if (notificationTemplateRepository.existsByNameIgnoreCase(request.getName())) {
			throw new BusinessValidationException("Template already exists: " + request.getName());
		}

		NotificationTemplate template = NotificationTemplate.builder()
				.name(request.getName())
				.channel(request.getChannel())
				.subject(request.getSubject())
				.body(request.getBody())
				.build();
		return notificationTemplateRepository.save(template);
	}

	public NotificationTemplate getTemplateById(UUID templateId) {
		return notificationTemplateRepository.findById(templateId)
				.orElseThrow(() -> new ResourceNotFoundException("Template not found: " + templateId));
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}
}
