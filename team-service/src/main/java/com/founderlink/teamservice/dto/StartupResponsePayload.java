package com.founderlink.teamservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Maps startup-service {@code GET /startups/{id}} body ({@code startup} + optional {@code founder}).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StartupResponsePayload {

	private StartupDto startup;
}
