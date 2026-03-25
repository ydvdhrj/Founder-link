package com.founderlink.teamservice.exception;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Map<String, String>> badRequest(IllegalArgumentException e) {
		String msg = e.getMessage() != null ? e.getMessage() : "Bad request";
		if (msg.startsWith("Startup not found") || msg.startsWith("Invitation not found")) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", msg));
		}
		if (msg.startsWith("Only the startup founder")) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", msg));
		}
		return ResponseEntity.badRequest().body(Map.of("error", msg));
	}

	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<Map<String, String>> state(IllegalStateException e) {
		String msg = e.getMessage() != null ? e.getMessage() : "Error";
		if (msg.startsWith("Could not load startup")) {
			return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of("error", msg));
		}
		return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", msg));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, String>> validation(MethodArgumentNotValidException e) {
		String msg = e.getBindingResult().getFieldErrors().stream()
				.map(err -> err.getField() + ": " + err.getDefaultMessage())
				.findFirst()
				.orElse("Validation failed");
		return ResponseEntity.badRequest().body(Map.of("error", msg));
	}
}
