export type ReportStatus = 'DRAFT' | 'PUBLISHED';

export interface Report {
  reportId: number;
  patientId: number;
  patientName: string;
  planId: number;
  planName: string;
  generatedBy: number;
  summary: string;
  adminSummary: string;
  date: string;
  status: ReportStatus;
  totalActivities: number;
  completedActivities: number;
  completionPercentage: number;
}

export interface ReportPreview {
  patientId: number;
  patientName: string;
  planId: number;
  planName: string;
  totalActivities: number;
  completedActivities: number;
  completionPercentage: number;
  dateRange: string;
}

export interface GenerateReportRequest {
  adminId: number;
  adminSummary: string;
}
