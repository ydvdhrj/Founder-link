package com.founderlink.notification_service.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.founderlink.notification_service.model.NotificationTemplate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest(properties = {
		"spring.config.import=optional:classpath:/",
		"spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
		"spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
@ActiveProfiles("test")
class NotificationTemplateRepositoryTest {

	@Autowired
	private NotificationTemplateRepository notificationTemplateRepository;

	@Test
	void existsByNameIgnoreCase_shouldReturnTrue_whenTemplateExists() {
		// given
		NotificationTemplate welcome = NotificationTemplate.builder()
				.name("WELCOME_EMAIL")
				.channel("EMAIL")
				.subject("Welcome")
				.body("Hello")
				.build();
		notificationTemplateRepository.save(welcome);

		// when
		boolean exists = notificationTemplateRepository.existsByNameIgnoreCase("welcome_email");

		// then
		assertThat(exists).isTrue();
	}

	@Test
	void findByChannelIgnoreCase_shouldReturnOnlyMatchingTemplates() {
		// given
		notificationTemplateRepository.save(NotificationTemplate.builder()
				.name("PITCH_APPROVED")
				.channel("EMAIL")
				.subject("Approved")
				.body("Congrats")
				.build());
		notificationTemplateRepository.save(NotificationTemplate.builder()
				.name("PITCH_REJECTED")
				.channel("EMAIL")
				.subject("Rejected")
				.body("Try again")
				.build());
		notificationTemplateRepository.save(NotificationTemplate.builder()
				.name("SMS_OTP")
				.channel("SMS")
				.subject("OTP")
				.body("123456")
				.build());

		// when
		List<NotificationTemplate> result = notificationTemplateRepository.findByChannelIgnoreCase("email");

		// then
		assertThat(result).hasSize(2);
		assertThat(result).extracting(NotificationTemplate::getName)
				.containsExactlyInAnyOrder("PITCH_APPROVED", "PITCH_REJECTED");
	}
}
