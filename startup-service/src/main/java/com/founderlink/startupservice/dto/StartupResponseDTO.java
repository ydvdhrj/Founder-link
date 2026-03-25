package com.founderlink.startupservice.dto;

import com.founderlink.startupservice.entity.Startup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StartupResponseDTO {

	private Startup startup;
	private UserDto founder;
}
