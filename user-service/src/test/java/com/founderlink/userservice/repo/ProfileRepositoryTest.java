package com.founderlink.userservice.repo;

import static org.assertj.core.api.Assertions.assertThat;

import com.founderlink.userservice.entity.Profile;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
class ProfileRepositoryTest {

	@Autowired
	private ProfileRepository profileRepository;

	@Test
	void findByUserId_shouldReturnProfile_whenRecordExists() {
		// given
		Profile profile = new Profile();
		profile.setUserId(1001L);
		profile.setName("Alice Founder");
		profile.setEmail("alice@founderlink.com");
		profile.setSkills("Java,Spring");
		profile.setExperience("5 years");
		profile.setBio("Building products");
		profile.setPortfolioLinks("[\"https://portfolio.example/alice\"]");
		profileRepository.save(profile);

		// when
		Optional<Profile> result = profileRepository.findByUserId(1001L);

		// then
		assertThat(result).isPresent();
		assertThat(result.get().getUserId()).isEqualTo(1001L);
		assertThat(result.get().getEmail()).isEqualTo("alice@founderlink.com");
	}

	@Test
	void existsByUserId_shouldReturnTrue_whenRecordExists() {
		// given
		Profile profile = new Profile();
		profile.setUserId(2002L);
		profile.setName("Bob Investor");
		profile.setEmail("bob@founderlink.com");
		profileRepository.save(profile);

		// when
		boolean exists = profileRepository.existsByUserId(2002L);

		// then
		assertThat(exists).isTrue();
	}

	@Test
	void existsByUserId_shouldReturnFalse_whenRecordDoesNotExist() {
		// when
		boolean exists = profileRepository.existsByUserId(9999L);

		// then
		assertThat(exists).isFalse();
	}
}
