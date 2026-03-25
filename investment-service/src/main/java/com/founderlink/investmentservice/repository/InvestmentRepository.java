package com.founderlink.investmentservice.repository;

import com.founderlink.investmentservice.entity.Investment;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvestmentRepository extends JpaRepository<Investment, UUID> {

	List<Investment> findByStartupId(String startupId);

	List<Investment> findByInvestorId(String investorId);
}
