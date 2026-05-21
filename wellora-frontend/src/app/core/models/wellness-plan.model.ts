export interface ActivityDTO {
  activityId: number;
  activityName: string;
}

export interface WellnessPlan {
  planId: number;
  planName: string;
  activities: ActivityDTO[];
}

export interface CreateWellnessPlanRequest {
  planName: string;
  activityNames: string[];
}

export interface PlanAssignment {
  assignmentId: number;
  patientId: number;
  planId: number;
  planName: string;
  assignedDate: string;
  status: 'ACTIVE' | 'INACTIVE';
}
