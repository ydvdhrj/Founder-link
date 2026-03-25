package com.founderlink.investmentservice.client;

import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Verifies a startup exists via startup-service {@code GET /startups/{id}}.
 * Response is deserialized as a map (spec: {@code Object}); shape matches {@code StartupResponseDTO} JSON.
 */
@FeignClient(name = "startup-service", path = "/startups")
public interface StartupServiceClient {

	@GetMapping("/{id}")
	Map<String, Object> getStartupById(@PathVariable("id") String startupId);
}
