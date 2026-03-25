package com.founderlink.teamservice.dto;

import com.founderlink.teamservice.entity.TeamRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InviteMemberRequest {

	@NotBlank
	private String startupId;

	@NotBlank
	private String invitedUserId;

	@NotNull
	private TeamRole role;
}
