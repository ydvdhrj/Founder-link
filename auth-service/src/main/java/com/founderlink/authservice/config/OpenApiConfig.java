package com.founderlink.authservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger UI is aggregated at the API Gateway. If this spec advertises {@code http://localhost:8081},
 * the browser sends cross-origin requests from the Swagger page (8080) to the auth port (8081) and
 * blocks them (no CORS on downstream services). A relative server URL keeps "Try it out" on the
 * gateway origin.
 */
@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI openAPI() {
		return new OpenAPI()
				.servers(List.of(new Server().url("/").description("API Gateway (same origin as Swagger UI)")));
	}
}
