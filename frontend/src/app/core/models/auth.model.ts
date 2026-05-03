export type RoleName =
  | 'ROLE_FOUNDER'
  | 'ROLE_INVESTOR'
  | 'ROLE_COFOUNDER'
  | 'ROLE_ADMIN';

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
  role: RoleName;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresInSeconds: number;
  userId: number;
  email: string;
  name: string;
  roles: string[];
}

export interface UserResponse {
  id: number;
  name: string;
  email: string;
  roles: string[];
  createdAt: string;
}

export interface AuthState {
  accessToken: string;
  refreshToken: string;
  userId: number;
  email: string;
  name: string;
  roles: string[];
  expiresAt: number;
}
