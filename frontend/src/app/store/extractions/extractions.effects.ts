import { Injectable } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { catchError, map, switchMap } from 'rxjs/operators';
import { of } from 'rxjs';
import * as ExtractionsActions from './extractions.actions';
import { ExtractionService } from '../../core/services/extraction.service';

@Injectable()
export class ExtractionsEffects {
  loadExtraction$ = createEffect(() =>
    this.actions$.pipe(
      ofType(ExtractionsActions.loadExtraction),
      switchMap(({ documentId }) =>
        this.extractionService.getExtractionByDocument(documentId).pipe(
          map(extraction => ExtractionsActions.loadExtractionSuccess({ extraction })),
          catchError(error => of(ExtractionsActions.loadExtractionFailure({ error: error.message })))
        )
      )
    )
  );

  approveExtraction$ = createEffect(() =>
    this.actions$.pipe(
      ofType(ExtractionsActions.approveExtraction),
      switchMap(({ extractionId }) =>
        this.extractionService.approveExtraction(extractionId).pipe(
          map(() => ExtractionsActions.approveExtractionSuccess()),
          catchError(error => of(ExtractionsActions.loadExtractionFailure({ error: error.message })))
        )
      )
    )
  );

  constructor(
    private actions$: Actions,
    private extractionService: ExtractionService
  ) {}
}
