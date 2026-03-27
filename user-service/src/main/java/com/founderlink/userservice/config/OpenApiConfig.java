package com.founderlink.userservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Relative server URL so Swagger UI at the gateway does not call this service's port directly (avoids CORS).
 */
@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI openAPI() {
		return new OpenAPI()
				.servers(List.of(new Server().url("/").description("API Gateway (same origin as Swagger UI)")));
	}
}
