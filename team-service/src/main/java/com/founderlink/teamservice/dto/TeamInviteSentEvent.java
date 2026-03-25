package com.founderlink.teamservice.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamInviteSentEvent {

	public static final String TYPE = "TEAM_INVITE_SENT";

	private String eventType;
	private UUID inviteId;
	private String startupId;
	private String invitedUserId;
	private String role;
	private String requesterId;
}
