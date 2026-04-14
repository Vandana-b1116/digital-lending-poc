import { createReducer, on } from '@ngrx/store';
import { LoanApplication } from './applications.model';
import * as ApplicationsActions from './applications.actions';

export interface ApplicationsState {
  list: LoanApplication[];
  selected: LoanApplication | null;
  loading: boolean;
  error: string | null;
}

const initialState: ApplicationsState = {
  list: [],
  selected: null,
  loading: false,
  error: null,
};

export const applicationsReducer = createReducer(
  initialState,
  on(ApplicationsActions.loadApplications, state => ({ ...state, loading: true, error: null })),
  on(ApplicationsActions.loadApplicationsSuccess, (state, { applications }) => ({
    ...state, loading: false, list: applications,
  })),
  on(ApplicationsActions.loadApplicationsFailure, (state, { error }) => ({
    ...state, loading: false, error,
  })),
  on(ApplicationsActions.loadApplicationSuccess, (state, { application }) => ({
    ...state, loading: false, selected: application,
  })),
  on(ApplicationsActions.createApplicationSuccess, (state, { application }) => ({
    ...state, list: [...state.list, application],
  }))
);
