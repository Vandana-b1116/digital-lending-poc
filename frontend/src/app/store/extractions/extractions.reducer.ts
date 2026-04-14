import { createReducer, on } from '@ngrx/store';
import { ExtractionResult } from './extractions.model';
import * as ExtractionsActions from './extractions.actions';

export interface ExtractionsState {
  current: ExtractionResult | null;
  loading: boolean;
  approving: boolean;
  error: string | null;
}

const initialState: ExtractionsState = {
  current: null,
  loading: false,
  approving: false,
  error: null,
};

export const extractionsReducer = createReducer(
  initialState,
  on(ExtractionsActions.loadExtraction, state => ({ ...state, loading: true, error: null })),
  on(ExtractionsActions.loadExtractionSuccess, (state, { extraction }) => ({
    ...state, loading: false, current: extraction,
  })),
  on(ExtractionsActions.loadExtractionFailure, (state, { error }) => ({
    ...state, loading: false, error,
  })),
  on(ExtractionsActions.approveExtraction, state => ({ ...state, approving: true })),
  on(ExtractionsActions.approveExtractionSuccess, state => ({ ...state, approving: false }))
);
