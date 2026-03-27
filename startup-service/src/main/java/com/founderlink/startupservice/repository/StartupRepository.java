package com.founderlink.startupservice.repository;

import com.founderlink.startupservice.entity.Startup;
import com.founderlink.startupservice.entity.StartupStage;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StartupRepository extends JpaRepository<Startup, UUID> {

	boolean existsByNameAndFounderId(String name, String founderId);

	List<Startup> findByIndustryAndStage(String industry, StartupStage stage);
}
