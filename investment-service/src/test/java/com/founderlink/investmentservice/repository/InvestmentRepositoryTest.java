package com.founderlink.investmentservice.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.founderlink.investmentservice.entity.Investment;
import com.founderlink.investmentservice.entity.InvestmentStatus;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest(properties = {
		"spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
		"spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
@ActiveProfiles("test")
class InvestmentRepositoryTest {

	@Autowired
	private InvestmentRepository investmentRepository;

	@Test
	void findByStartupId_shouldReturnOnlyMatchingInvestments() {
		// given
		Investment a = Investment.builder()
				.startupId("startup-1")
				.investorId("investor-a")
				.amount(10000.0)
				.status(InvestmentStatus.PENDING)
				.build();
		Investment b = Investment.builder()
				.startupId("startup-1")
				.investorId("investor-b")
				.amount(20000.0)
				.status(InvestmentStatus.APPROVED)
				.build();
		Investment other = Investment.builder()
				.startupId("startup-2")
				.investorId("investor-c")
				.amount(30000.0)
				.status(InvestmentStatus.PENDING)
				.build();
		investmentRepository.save(a);
		investmentRepository.save(b);
		investmentRepository.save(other);

		// when
		List<Investment> result = investmentRepository.findByStartupId("startup-1");

		// then
		assertThat(result).hasSize(2);
		assertThat(result).extracting(Investment::getInvestorId)
				.containsExactlyInAnyOrder("investor-a", "investor-b");
	}

	@Test
	void findByInvestorId_shouldReturnOnlyMatchingInvestments() {
		// given
		Investment a = Investment.builder()
				.startupId("startup-x")
				.investorId("investor-77")
				.amount(15000.0)
				.status(InvestmentStatus.PENDING)
				.build();
		Investment b = Investment.builder()
				.startupId("startup-y")
				.investorId("investor-77")
				.amount(25000.0)
				.status(InvestmentStatus.COMPLETED)
				.build();
		Investment other = Investment.builder()
				.startupId("startup-z")
				.investorId("investor-88")
				.amount(35000.0)
				.status(InvestmentStatus.REJECTED)
				.build();
		investmentRepository.save(a);
		investmentRepository.save(b);
		investmentRepository.save(other);

		// when
		List<Investment> result = investmentRepository.findByInvestorId("investor-77");

		// then
		assertThat(result).hasSize(2);
		assertThat(result).extracting(Investment::getStartupId)
				.containsExactlyInAnyOrder("startup-x", "startup-y");
	}
}
