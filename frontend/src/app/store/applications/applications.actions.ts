import { createAction, props } from '@ngrx/store';
import { LoanApplication } from './applications.model';

export const loadApplications = createAction('[Applications] Load Applications');
export const loadApplicationsSuccess = createAction(
  '[Applications] Load Applications Success',
  props<{ applications: LoanApplication[] }>()
);
export const loadApplicationsFailure = createAction(
  '[Applications] Load Applications Failure',
  props<{ error: string }>()
);

export const loadApplication = createAction(
  '[Applications] Load Application',
  props<{ id: number }>()
);
export const loadApplicationSuccess = createAction(
  '[Applications] Load Application Success',
  props<{ application: LoanApplication }>()
);
export const loadApplicationFailure = createAction(
  '[Applications] Load Application Failure',
  props<{ error: string }>()
);

export const createApplication = createAction(
  '[Applications] Create Application',
  props<{ payload: Partial<LoanApplication> }>()
);
export const createApplicationSuccess = createAction(
  '[Applications] Create Application Success',
  props<{ application: LoanApplication }>()
);
