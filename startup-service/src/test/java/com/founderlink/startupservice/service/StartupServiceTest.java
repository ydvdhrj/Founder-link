package com.founderlink.startupservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.founderlink.startupservice.client.UserServiceClient;
import com.founderlink.startupservice.config.RabbitMQConfig;
import com.founderlink.startupservice.dto.CreateStartupRequest;
import com.founderlink.startupservice.dto.StartupCreatedEvent;
import com.founderlink.startupservice.dto.StartupResponseDTO;
import com.founderlink.startupservice.dto.UserDto;
import com.founderlink.startupservice.entity.Startup;
import com.founderlink.startupservice.entity.StartupStage;
import com.founderlink.startupservice.exception.BusinessValidationException;
import com.founderlink.startupservice.exception.ResourceNotFoundException;
import com.founderlink.startupservice.repository.StartupRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@ExtendWith(MockitoExtension.class)
class StartupServiceTest {

	@Mock
	private StartupRepository startupRepository;

	@Mock
	private UserServiceClient userServiceClient;

	@Mock
	private RabbitTemplate rabbitTemplate;

	@InjectMocks
	private StartupService startupService;

	@Test
	void createStartup_shouldSaveAndPublishEvent_whenRequestIsValid() {
		// Given
		String founderId = "founder-123";
		CreateStartupRequest request = new CreateStartupRequest();
		request.setName("FounderLink AI");
		request.setDescription("AI-powered founder platform");
		request.setIndustry("SaaS");
		request.setStage(StartupStage.MVP);
		request.setFundingGoal(250000.0);

		Startup saved = Startup.builder()
				.id(UUID.randomUUID())
				.name(request.getName())
				.description(request.getDescription())
				.industry(request.getIndustry())
				.stage(request.getStage())
				.fundingGoal(request.getFundingGoal())
				.founderId(founderId)
				.build();

		given(startupRepository.save(any(Startup.class))).willReturn(saved);

		// When
		Startup result = startupService.createStartup(request, founderId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(saved.getId());
		assertThat(result.getName()).isEqualTo("FounderLink AI");
		assertThat(result.getFounderId()).isEqualTo(founderId);

		ArgumentCaptor<Startup> startupCaptor = ArgumentCaptor.forClass(Startup.class);
		then(startupRepository).should().save(startupCaptor.capture());
		assertThat(startupCaptor.getValue().getName()).isEqualTo(request.getName());
		assertThat(startupCaptor.getValue().getFounderId()).isEqualTo(founderId);

		then(rabbitTemplate).should()
				.convertAndSend(
						eq(RabbitMQConfig.FOUNDERLINK_EXCHANGE),
						eq(StartupService.ROUTING_KEY_STARTUP_CREATED),
						any(StartupCreatedEvent.class));
	}

	@Test
	void getStartupWithFounderDetails_shouldThrowResourceNotFound_whenStartupMissing() {
		// Given
		UUID startupId = UUID.randomUUID();
		given(startupRepository.findById(startupId)).willReturn(Optional.empty());

		// When / Then
		assertThatThrownBy(() -> startupService.getStartupWithFounderDetails(startupId.toString()))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessageContaining("Startup not found");

		then(userServiceClient).shouldHaveNoInteractions();
	}

	@Test
	void getStartupWithFounderDetails_shouldReturnStartupAndFounder_whenStartupExists() {
		// Given
		UUID startupId = UUID.randomUUID();
		String founderId = "42";
		Long founderUserId = 42L;
		Startup startup = Startup.builder()
				.id(startupId)
				.name("Growth AI")
				.description("Growth analytics")
				.industry("SaaS")
				.stage(StartupStage.EARLY_TRACTION)
				.fundingGoal(500000.0)
				.founderId(founderId)
				.build();
		UserDto founder = UserDto.builder()
				.userId(founderUserId)
				.name("Founder User")
				.email("founder@example.com")
				.build();

		given(startupRepository.findById(startupId)).willReturn(Optional.of(startup));
		given(userServiceClient.getUserById(founderId)).willReturn(founder);

		// When
		StartupResponseDTO response = startupService.getStartupWithFounderDetails(startupId.toString());

		// Then
		assertThat(response).isNotNull();
		assertThat(response.getStartup()).isNotNull();
		assertThat(response.getStartup().getId()).isEqualTo(startupId);
		assertThat(response.getFounder()).isNotNull();
		assertThat(response.getFounder().getUserId()).isEqualTo(founderUserId);
		then(startupRepository).should().findById(startupId);
		then(userServiceClient).should().getUserById(founderId);
	}

	@Test
	void getStartupWithFounderDetails_shouldThrowBusinessValidation_whenIdFormatInvalid() {
		// Given
		String invalidId = "not-a-uuid";

		// When / Then
		assertThatThrownBy(() -> startupService.getStartupWithFounderDetails(invalidId))
				.isInstanceOf(BusinessValidationException.class)
				.hasMessageContaining("Invalid startup id format");

		then(startupRepository).shouldHaveNoInteractions();
		then(userServiceClient).shouldHaveNoInteractions();
	}
}
