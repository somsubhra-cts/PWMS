export interface Patient {
  patientId: number;
  patientName: string;
  age: number;
  email: string;
  medicalHistory: string | null;
}

export interface PatientRequest {
  patientName: string;
  age: number;
  email: string;
  medicalHistory: string | null;
}
