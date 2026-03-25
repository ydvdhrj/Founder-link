package com.founderlink.startupservice.exception;

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
		if (e.getMessage() != null && e.getMessage().startsWith("Startup not found")) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
		}
		return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
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
