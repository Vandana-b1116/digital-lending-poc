import { createAction, props } from '@ngrx/store';
import { ExtractionResult, FieldApproval } from './extractions.model';

export const loadExtraction = createAction(
  '[Extractions] Load Extraction',
  props<{ documentId: number }>()
);
export const loadExtractionSuccess = createAction(
  '[Extractions] Load Extraction Success',
  props<{ extraction: ExtractionResult }>()
);
export const loadExtractionFailure = createAction(
  '[Extractions] Load Extraction Failure',
  props<{ error: string }>()
);

export const submitFieldApprovals = createAction(
  '[Extractions] Submit Field Approvals',
  props<{ extractionId: number; approvals: FieldApproval[] }>()
);
export const submitFieldApprovalsSuccess = createAction(
  '[Extractions] Submit Field Approvals Success'
);

export const approveExtraction = createAction(
  '[Extractions] Approve Extraction',
  props<{ extractionId: number }>()
);
export const approveExtractionSuccess = createAction(
  '[Extractions] Approve Extraction Success'
);
