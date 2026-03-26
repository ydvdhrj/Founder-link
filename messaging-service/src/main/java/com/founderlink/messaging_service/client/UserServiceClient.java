package com.founderlink.messaging_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", path = "/users")
public interface UserServiceClient {

	@GetMapping("/{id}")
	Object getUserById(@PathVariable("id") String userId);
}

