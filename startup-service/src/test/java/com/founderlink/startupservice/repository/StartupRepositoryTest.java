package com.founderlink.startupservice.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.founderlink.startupservice.entity.Startup;
import com.founderlink.startupservice.entity.StartupStage;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest(properties = {
		"spring.cloud.config.enabled=false",
		"spring.config.import=",
		"spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
		"spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
@ActiveProfiles("test")
class StartupRepositoryTest {

	@Autowired
	private StartupRepository startupRepository;

	@Test
	void findByIndustryAndStage_shouldReturnMatchingStartupsOnly() {
		// given
		Startup matchingA = Startup.builder()
				.name("SaaS One")
				.description("desc")
				.industry("SaaS")
				.stage(StartupStage.MVP)
				.fundingGoal(10000.0)
				.founderId("founder-1")
				.build();
		Startup matchingB = Startup.builder()
				.name("SaaS Two")
				.description("desc")
				.industry("SaaS")
				.stage(StartupStage.MVP)
				.fundingGoal(20000.0)
				.founderId("founder-2")
				.build();
		Startup nonMatching = Startup.builder()
				.name("Different Stage")
				.description("desc")
				.industry("FinTech")
				.stage(StartupStage.MVP)
				.fundingGoal(30000.0)
				.founderId("founder-3")
				.build();
		startupRepository.save(matchingA);
		startupRepository.save(matchingB);
		startupRepository.save(nonMatching);

		// when
		List<Startup> result = startupRepository.findByIndustryAndStage("SaaS", StartupStage.MVP);

		// then
		assertThat(result).hasSize(2);
		assertThat(result).extracting(Startup::getName).containsExactlyInAnyOrder("SaaS One", "SaaS Two");
		assertThat(result).allMatch(startup -> "SaaS".equals(startup.getIndustry()));
		assertThat(result).allMatch(startup -> startup.getStage() == StartupStage.MVP);
	}
}
