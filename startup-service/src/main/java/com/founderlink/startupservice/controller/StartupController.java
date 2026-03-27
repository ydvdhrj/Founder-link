package com.founderlink.startupservice.controller;

import com.founderlink.startupservice.dto.CreateStartupRequest;
import com.founderlink.startupservice.dto.StartupResponseDTO;
import com.founderlink.startupservice.entity.Startup;
import com.founderlink.startupservice.service.StartupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/startups")
@RequiredArgsConstructor
@Tag(name = "Startups", description = "Startup listings (founder id from API Gateway header X-User-Id)")
public class StartupController {

	private final StartupService startupService;

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Create startup", description = "Creates a startup for the authenticated user; publishes STARTUP_CREATED to RabbitMQ.")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "Created",
					content = @Content(schema = @Schema(implementation = Startup.class))),
			@ApiResponse(responseCode = "400", description = "Validation error")
	})
	public ResponseEntity<Startup> create(
			@Valid @RequestBody CreateStartupRequest request,
			@Parameter(description = "Auth user id propagated by API Gateway", required = true)
			@RequestHeader("X-User-Id") String founderId) {
		Startup saved = startupService.createStartup(request, founderId);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(saved.getId())
				.toUri();
		return ResponseEntity.created(location).body(saved);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get startup with founder", description = "Loads startup and founder profile via user-service.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "OK",
					content = @Content(schema = @Schema(implementation = StartupResponseDTO.class))),
			@ApiResponse(responseCode = "400", description = "Invalid id"),
			@ApiResponse(responseCode = "404", description = "Startup not found",
					headers = @Header(name = "X-Error", description = "Error detail"))
	})
	public StartupResponseDTO getById(@PathVariable String id) {
		return startupService.getStartupWithFounderDetails(id);
	}

	@GetMapping
	@Operation(summary = "List all startups")
	@ApiResponse(responseCode = "200", description = "OK",
			content = @Content(schema = @Schema(implementation = Startup.class)))
	public List<Startup> list() {
		return startupService.getAllStartups();
	}
}
