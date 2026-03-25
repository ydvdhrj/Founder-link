package com.founderlink.teamservice.client;

import com.founderlink.teamservice.dto.StartupResponsePayload;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * startup-service exposes {@code GET /startups/{id}} with a nested {@code startup} object.
 * Use {@link StartupResponsePayload#getStartup()} as the effective {@code StartupDto}.
 */
@FeignClient(name = "startup-service", path = "/startups")
public interface StartupServiceClient {

	@GetMapping("/{id}")
	StartupResponsePayload getStartupById(@PathVariable("id") String startupId);
}
