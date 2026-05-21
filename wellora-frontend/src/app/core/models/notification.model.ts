export type ReceiverType = 'PATIENT' | 'ADMIN';

export type PatientNotificationType =
  | 'ACTIVITY_REMINDER'
  | 'ACTIVITY_APPRECIATION'
  | 'PLAN_ASSIGNED'
  | 'APPOINTMENT_REMINDER'
  | 'REPORT_SHARED'
  | 'WEEKLY_SUMMARY';

export type AdminNotificationType =
  | 'NEW_PATIENT_REGISTERED'
  | 'PLAN_COMPLETED'
  | 'GENERATE_REPORT_REMINDER';

export type NotificationType = PatientNotificationType | AdminNotificationType;

export interface Notification {
  notificationId: number;
  receiverId: number;
  receiverType: ReceiverType;
  notificationType: NotificationType;
  message: string;
  patientId: number | null;
  planId: number | null;
  isRead: boolean;
  createdAt: string;
}
