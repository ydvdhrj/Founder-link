package com.founderlink.startupservice.client;

import com.founderlink.startupservice.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", path = "/users")
public interface UserServiceClient {

	@GetMapping("/{id}")
	UserDto getUserById(@PathVariable("id") String userId);
}
