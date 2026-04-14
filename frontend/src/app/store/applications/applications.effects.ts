import { Injectable } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { catchError, map, switchMap } from 'rxjs/operators';
import { of } from 'rxjs';
import * as ApplicationsActions from './applications.actions';
import { ApplicationService } from '../../core/services/application.service';

@Injectable()
export class ApplicationsEffects {
  loadApplications$ = createEffect(() =>
    this.actions$.pipe(
      ofType(ApplicationsActions.loadApplications),
      switchMap(() =>
        this.applicationService.getApplications().pipe(
          map(applications => ApplicationsActions.loadApplicationsSuccess({ applications })),
          catchError(error => of(ApplicationsActions.loadApplicationsFailure({ error: error.message })))
        )
      )
    )
  );

  loadApplication$ = createEffect(() =>
    this.actions$.pipe(
      ofType(ApplicationsActions.loadApplication),
      switchMap(({ id }) =>
        this.applicationService.getApplication(id).pipe(
          map(application => ApplicationsActions.loadApplicationSuccess({ application })),
          catchError(error => of(ApplicationsActions.loadApplicationFailure({ error: error.message })))
        )
      )
    )
  );

  constructor(
    private actions$: Actions,
    private applicationService: ApplicationService
  ) {}
}
