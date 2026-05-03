export interface ProfileResponse {
  id: number;
  userId: number;
  name: string;
  email: string;
  skills?: string;
  experience?: string;
  bio?: string;
  portfolioLinks?: string[];
  createdAt: string;
  updatedAt: string;
}

export interface CreateProfileRequest {
  name: string;
  email: string;
  skills?: string;
  experience?: string;
  bio?: string;
  portfolioLinks?: string[];
}

export type UpdateProfileRequest = Partial<CreateProfileRequest>;

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}
