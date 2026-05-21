export type ProgressStatus = 'DONE' | 'PENDING' | 'SKIPPED';

export interface ProgressRecord {
  progressId: number;
  patientId: number;
  planId: number;
  activityId: number;
  activityName: string;
  status: ProgressStatus;
  trackedDate: string;
}

export interface StatusUpdateRequest {
  activityId: number;
  status: ProgressStatus;
  planId: number;
}

export interface ProgressSummary {
  patientId: number;
  planId: number;
  date: string;
  totalActivities: number;
  completedActivities: number;
  completionPercentage: number;
}
