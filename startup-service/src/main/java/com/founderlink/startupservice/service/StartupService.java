package com.founderlink.startupservice.service;

import com.founderlink.startupservice.client.UserServiceClient;
import com.founderlink.startupservice.config.RabbitMQConfig;
import com.founderlink.startupservice.dto.CreateStartupRequest;
import com.founderlink.startupservice.dto.StartupCreatedEvent;
import com.founderlink.startupservice.dto.StartupResponseDTO;
import com.founderlink.startupservice.dto.UserDto;
import com.founderlink.startupservice.entity.Startup;
import com.founderlink.startupservice.repository.StartupRepository;
import feign.FeignException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StartupService {

	public static final String ROUTING_KEY_STARTUP_CREATED = "startup.created";

	private final StartupRepository startupRepository;
	private final RabbitTemplate rabbitTemplate;
	private final UserServiceClient userServiceClient;

	@Transactional
	public Startup createStartup(CreateStartupRequest request, String founderId) {
		Startup startup = Startup.builder()
				.name(request.getName())
				.description(request.getDescription())
				.industry(request.getIndustry())
				.stage(request.getStage())
				.fundingGoal(request.getFundingGoal())
				.founderId(founderId)
				.build();
		Startup saved = startupRepository.save(startup);

		StartupCreatedEvent event = StartupCreatedEvent.builder()
				.eventType(StartupCreatedEvent.TYPE)
				.startupId(saved.getId())
				.founderId(saved.getFounderId())
				.industry(saved.getIndustry())
				.fundingGoal(saved.getFundingGoal())
				.build();
		rabbitTemplate.convertAndSend(RabbitMQConfig.FOUNDERLINK_EXCHANGE, ROUTING_KEY_STARTUP_CREATED, event);

		return saved;
	}

	@Transactional(readOnly = true)
	public StartupResponseDTO getStartupWithFounderDetails(String startupId) {
		UUID id = UUID.fromString(startupId);
		Startup startup = startupRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Startup not found: " + startupId));

		UserDto founder = null;
		try {
			founder = userServiceClient.getUserById(startup.getFounderId());
		} catch (FeignException.NotFound e) {
			log.warn("Founder profile not found for userId={}", startup.getFounderId());
		} catch (FeignException e) {
			log.warn("Could not load founder from user-service: {}", e.getMessage());
		}

		return StartupResponseDTO.builder().startup(startup).founder(founder).build();
	}

	@Transactional(readOnly = true)
	public List<Startup> getAllStartups() {
		return startupRepository.findAll();
	}
}
