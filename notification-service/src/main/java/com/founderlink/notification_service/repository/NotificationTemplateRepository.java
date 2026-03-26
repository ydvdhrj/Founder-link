package com.founderlink.notification_service.repository;

import com.founderlink.notification_service.model.NotificationTemplate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, UUID> {

	boolean existsByNameIgnoreCase(String name);

	Optional<NotificationTemplate> findByNameIgnoreCase(String name);

	List<NotificationTemplate> findByChannelIgnoreCase(String channel);
}
