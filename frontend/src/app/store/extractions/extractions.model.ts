export type ExtractionStatus = 'PENDING_REVIEW' | 'APPROVED' | 'REJECTED' | 'PARTIALLY_APPROVED';
export type DiscrepancySeverity = 'LOW' | 'MEDIUM' | 'HIGH';
export type DiscrepancyFlagType = 'INCOME_MISMATCH' | 'EMPLOYER_MISMATCH' | 'MISSING_FIELD';
export type OfficerAction = 'ACCEPTED' | 'EDITED' | 'REJECTED';

export interface DiscrepancyFlag {
  id: number;
  fieldName: string;
  extractedValue: string;
  selfReportedValue: string;
  variancePercentage: number;
  severity: DiscrepancySeverity;
  flagType: DiscrepancyFlagType;
}

export interface FieldApproval {
  fieldName: string;
  finalValue: string;
  officerAction: OfficerAction;
  officerNotes?: string;
}

export interface ExtractionResult {
  id: number;
  documentId: number;
  applicationId: number;
  employerName: string | null;
  payPeriodStart: string | null;
  payPeriodEnd: string | null;
  grossPay: number | null;
  netPay: number | null;
  ytdGross: number | null;
  ytdNet: number | null;
  payFrequency: string | null;
  confidenceScore: number | null;
  status: ExtractionStatus;
  discrepancyFlags: DiscrepancyFlag[];
  createdAt: string;
}
