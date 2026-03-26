package com.founderlink.startupservice.exception;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
	public ProblemDetail handleResourceNotFound(ResourceNotFoundException ex) {
		ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
		detail.setTitle("Resource not found");
		detail.setDetail(ex.getMessage());
		detail.setType(URI.create("about:blank"));
		return detail;
	}

	@ExceptionHandler(BusinessValidationException.class)
	public ProblemDetail handleBusinessValidation(BusinessValidationException ex) {
		ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
		detail.setTitle("Business validation failed");
		detail.setDetail(ex.getMessage());
		detail.setType(URI.create("about:blank"));
		return detail;
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ProblemDetail handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
		ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
		detail.setTitle("Validation failed");
		detail.setDetail("One or more fields are invalid.");
		detail.setType(URI.create("about:blank"));
		Map<String, String> errors = new HashMap<>();
		for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
			errors.put(fieldError.getField(), fieldError.getDefaultMessage());
		}
		detail.setProperty("errors", errors);
		return detail;
	}

	@ExceptionHandler(Exception.class)
	public ProblemDetail handleUnhandled(Exception ex) {
		ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
		detail.setTitle("Internal server error");
		detail.setDetail("An unexpected error occurred.");
		detail.setType(URI.create("about:blank"));
		return detail;
	}
}
