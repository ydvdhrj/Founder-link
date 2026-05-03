import { ProfileResponse } from './user.model';

export type StartupStage = 'IDEA' | 'MVP' | 'EARLY_TRACTION' | 'SCALING';

export interface Startup {
  id: string;
  name: string;
  description?: string;
  industry?: string;
  stage: StartupStage;
  fundingGoal: number;
  founderId: string;
  createdAt?: string;
}

export interface CreateStartupRequest {
  name: string;
  description?: string;
  industry?: string;
  stage: StartupStage;
  fundingGoal: number;
}

export interface StartupResponseDTO {
  startup: Startup;
  founder: Partial<ProfileResponse> & { id?: number; name?: string; email?: string };
}
