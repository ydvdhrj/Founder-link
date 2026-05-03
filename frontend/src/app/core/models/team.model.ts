export type TeamRole = 'CTO' | 'CPO' | 'MARKETING_HEAD' | 'ENGINEERING_LEAD';

export type InviteStatus = 'PENDING' | 'ACCEPTED' | 'REJECTED';

export interface TeamMember {
  id: string;
  startupId: string;
  invitedUserId: string;
  invitedBy?: string;
  role: TeamRole;
  status: InviteStatus;
  createdAt?: string;
}

export interface InviteMemberRequest {
  startupId: string;
  invitedUserId: string;
  role: TeamRole;
}
