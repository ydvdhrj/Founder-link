package com.founderlink.startupservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Mirrors user-service {@code ProfileResponse} JSON for OpenFeign deserialization.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDto {

	private Long id;
	private Long userId;
	private String name;
	private String email;
	private String skills;
	private String experience;
	private String bio;
	private List<String> portfolioLinks;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
