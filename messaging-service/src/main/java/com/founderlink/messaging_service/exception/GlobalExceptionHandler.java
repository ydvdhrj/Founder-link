package com.founderlink.messaging_service.exception;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Map<String, String>> badRequest(IllegalArgumentException e) {
		String msg = e.getMessage() != null ? e.getMessage() : "Bad request";
		if (msg.startsWith("Receiver not found")) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", msg));
		}
		return ResponseEntity.badRequest().body(Map.of("error", msg));
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<Map<String, String>> notFound(ResourceNotFoundException e) {
		String msg = e.getMessage() != null ? e.getMessage() : "Resource not found";
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", msg));
	}

	@ExceptionHandler(BusinessValidationException.class)
	public ResponseEntity<Map<String, String>> businessValidation(BusinessValidationException e) {
		String msg = e.getMessage() != null ? e.getMessage() : "Validation failed";
		return ResponseEntity.badRequest().body(Map.of("error", msg));
	}

	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<Map<String, String>> badGateway(IllegalStateException e) {
		String msg = e.getMessage() != null ? e.getMessage() : "Upstream error";
		return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of("error", msg));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, String>> validation(MethodArgumentNotValidException e) {
		String msg = e.getBindingResult().getFieldErrors().stream()
				.map(err -> err.getField() + ": " + err.getDefaultMessage())
				.findFirst()
				.orElse("Validation failed");
		return ResponseEntity.badRequest().body(Map.of("error", msg));
	}

	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<Map<String, String>> responseStatus(ResponseStatusException e) {
		HttpStatus status = HttpStatus.resolve(e.getStatusCode().value());
		if (status == null) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		String msg = e.getReason() != null ? e.getReason() : status.getReasonPhrase();
		return ResponseEntity.status(status).body(Map.of("error", msg));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, String>> unexpected(Exception e) {
		log.error("Unhandled messaging-service exception", e);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(Map.of("error", "Unexpected error"));
	}
}
