package com.founderlink.notification_service.controller;

import com.founderlink.notification_service.dto.CreateNotificationTemplateRequest;
import com.founderlink.notification_service.model.NotificationTemplate;
import com.founderlink.notification_service.service.NotificationTemplateService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notifications/templates")
@RequiredArgsConstructor
public class NotificationTemplateController {

	private final NotificationTemplateService notificationTemplateService;

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<NotificationTemplate> create(@Valid @RequestBody CreateNotificationTemplateRequest request) {
		NotificationTemplate created = notificationTemplateService.createTemplate(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	@GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public NotificationTemplate getById(@PathVariable UUID id) {
		return notificationTemplateService.getTemplateById(id);
	}
}
