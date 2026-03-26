package com.founderlink.userservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.founderlink.userservice.dto.CreateProfileRequest;
import com.founderlink.userservice.dto.ProfileResponse;
import com.founderlink.userservice.entity.Profile;
import com.founderlink.userservice.repo.ProfileRepository;
import com.founderlink.userservice.security.JwtPrincipal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

	@Mock
	private ProfileRepository profileRepository;

	@Mock
	private ObjectMapper objectMapper;

	@InjectMocks
	private ProfileService profileService;

	@Test
	void create_shouldPersistAndReturnProfile_whenRequestIsValid() throws Exception {
		// given
		JwtPrincipal principal = new JwtPrincipal(10L, "founder@founderlink.com", List.of("ROLE_FOUNDER"));
		CreateProfileRequest request = new CreateProfileRequest();
		request.setName("  Alice Founder  ");
		request.setEmail("  ALICE@MAIL.COM ");
		request.setSkills(" Java, Spring ");
		request.setExperience(" 5 years ");
		request.setBio(" building startups ");
		request.setPortfolioLinks(List.of("https://portfolio.example/alice"));

		given(profileRepository.existsByUserId(10L)).willReturn(false);
		given(objectMapper.writeValueAsString(request.getPortfolioLinks()))
				.willReturn("[\"https://portfolio.example/alice\"]");

		Profile saved = new Profile();
		saved.setId(100L);
		saved.setUserId(10L);
		saved.setName("Alice Founder");
		saved.setEmail("alice@mail.com");
		saved.setSkills("Java, Spring");
		saved.setExperience("5 years");
		saved.setBio("building startups");
		saved.setPortfolioLinks("[\"https://portfolio.example/alice\"]");

		given(profileRepository.save(any(Profile.class))).willReturn(saved);

		// when
		ProfileResponse result = profileService.create(request, principal);

		// then
		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(100L);
		assertThat(result.getUserId()).isEqualTo(10L);
		assertThat(result.getName()).isEqualTo("Alice Founder");
		assertThat(result.getEmail()).isEqualTo("alice@mail.com");

		ArgumentCaptor<Profile> profileCaptor = ArgumentCaptor.forClass(Profile.class);
		then(profileRepository).should().save(profileCaptor.capture());
		assertThat(profileCaptor.getValue().getName()).isEqualTo("Alice Founder");
		assertThat(profileCaptor.getValue().getEmail()).isEqualTo("alice@mail.com");
		assertThat(profileCaptor.getValue().getUserId()).isEqualTo(10L);
	}

	@Test
	void getByUserId_shouldThrowNotFound_whenProfileDoesNotExist() {
		// given
		Long userId = 999L;
		given(profileRepository.findByUserId(userId)).willReturn(Optional.empty());

		// when / then
		assertThatThrownBy(() -> profileService.getByUserId(userId))
				.isInstanceOf(ResponseStatusException.class)
				.satisfies(ex -> {
					ResponseStatusException rse = (ResponseStatusException) ex;
					assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
					assertThat(rse.getReason()).isEqualTo("Profile not found");
				});
	}

	@Test
	void create_shouldThrowConflict_whenProfileAlreadyExistsForUser() {
		// given
		JwtPrincipal principal = new JwtPrincipal(22L, "user@founderlink.com", List.of("ROLE_FOUNDER"));
		CreateProfileRequest request = new CreateProfileRequest();
		request.setName("John");
		request.setEmail("john@founderlink.com");
		request.setPortfolioLinks(List.of());

		given(profileRepository.existsByUserId(eq(22L))).willReturn(true);

		// when / then
		assertThatThrownBy(() -> profileService.create(request, principal))
				.isInstanceOf(ResponseStatusException.class)
				.satisfies(ex -> {
					ResponseStatusException rse = (ResponseStatusException) ex;
					assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
					assertThat(rse.getReason()).isEqualTo("Profile already exists for this user");
				});

		then(profileRepository).should().existsByUserId(22L);
		then(profileRepository).shouldHaveNoMoreInteractions();
	}
}
