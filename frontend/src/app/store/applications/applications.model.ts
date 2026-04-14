export type LoanStatus =
  | 'SUBMITTED'
  | 'DOCUMENTS_UPLOADED'
  | 'AI_EXTRACTION_COMPLETE'
  | 'OFFICER_REVIEW_PENDING'
  | 'OFFICER_APPROVED'
  | 'SENT_TO_UNDERWRITING'
  | 'DECISION_RECEIVED';

export interface LoanApplication {
  id: number;
  applicantName: string;
  applicantEmail: string;
  selfReportedAnnualIncome: number;
  selfReportedEmployer: string;
  loanAmountRequested: number;
  status: LoanStatus;
  createdAt: string;
  updatedAt: string;
}
