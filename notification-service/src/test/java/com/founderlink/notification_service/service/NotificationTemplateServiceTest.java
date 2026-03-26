package com.founderlink.notification_service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.founderlink.notification_service.dto.CreateNotificationTemplateRequest;
import com.founderlink.notification_service.exception.BusinessValidationException;
import com.founderlink.notification_service.exception.ResourceNotFoundException;
import com.founderlink.notification_service.model.NotificationTemplate;
import com.founderlink.notification_service.repository.NotificationTemplateRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationTemplateServiceTest {

	@Mock
	private NotificationTemplateRepository notificationTemplateRepository;

	@InjectMocks
	private NotificationTemplateService notificationTemplateService;

	@Test
	void createTemplate_shouldSave_whenRequestIsValid() {
		// given
		CreateNotificationTemplateRequest request = CreateNotificationTemplateRequest.builder()
				.name("WELCOME_EMAIL")
				.channel("EMAIL")
				.subject("Welcome to FounderLink")
				.body("Hello {{name}}, welcome aboard!")
				.build();
		NotificationTemplate saved = NotificationTemplate.builder()
				.id(UUID.randomUUID())
				.name("WELCOME_EMAIL")
				.channel("EMAIL")
				.subject("Welcome to FounderLink")
				.body("Hello {{name}}, welcome aboard!")
				.build();

		given(notificationTemplateRepository.existsByNameIgnoreCase("WELCOME_EMAIL")).willReturn(false);
		given(notificationTemplateRepository.save(any(NotificationTemplate.class))).willReturn(saved);

		// when
		NotificationTemplate result = notificationTemplateService.createTemplate(request);

		// then
		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(saved.getId());
		assertThat(result.getName()).isEqualTo("WELCOME_EMAIL");
		then(notificationTemplateRepository).should().existsByNameIgnoreCase("WELCOME_EMAIL");
		then(notificationTemplateRepository).should().save(any(NotificationTemplate.class));
	}

	@Test
	void getTemplateById_shouldThrowNotFound_whenTemplateMissing() {
		// given
		UUID templateId = UUID.randomUUID();
		given(notificationTemplateRepository.findById(templateId)).willReturn(Optional.empty());

		// when / then
		assertThatThrownBy(() -> notificationTemplateService.getTemplateById(templateId))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessageContaining("Template not found");

		then(notificationTemplateRepository).should().findById(templateId);
	}

	@Test
	void createTemplate_shouldThrowBusinessValidation_whenTemplateNameAlreadyExists() {
		// given
		CreateNotificationTemplateRequest request = CreateNotificationTemplateRequest.builder()
				.name("WELCOME_EMAIL")
				.channel("EMAIL")
				.subject("Welcome")
				.body("Hello")
				.build();
		given(notificationTemplateRepository.existsByNameIgnoreCase("WELCOME_EMAIL")).willReturn(true);

		// when / then
		assertThatThrownBy(() -> notificationTemplateService.createTemplate(request))
				.isInstanceOf(BusinessValidationException.class)
				.hasMessageContaining("Template already exists");

		then(notificationTemplateRepository).should().existsByNameIgnoreCase("WELCOME_EMAIL");
		then(notificationTemplateRepository).shouldHaveNoMoreInteractions();
	}
}
