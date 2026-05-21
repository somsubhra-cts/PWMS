export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
  role: 'ADMIN' | 'PATIENT';
  referenceId: number;
  patientEmail: string;  // must match the email admin registered for this patient
}

export interface AuthResponse {
  token: string;
  username: string;
  role: 'ADMIN' | 'PATIENT';
  referenceId: number;
  expiresIn: number;
}

export interface DecodedToken {
  sub: string;
  role: 'ADMIN' | 'PATIENT';
  referenceId: number;
  iat: number;
  exp: number;
}
