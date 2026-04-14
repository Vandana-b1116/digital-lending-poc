import { Component, OnInit } from '@angular/core';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs';
import { LoanApplication } from '../../../store/applications/applications.model';
import { loadApplications } from '../../../store/applications/applications.actions';

@Component({
  selector: 'app-application-list',
  template: `
    <div class="card">
      <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:16px">
        <h2>Loan Applications</h2>
        <button class="btn btn-primary" (click)="showCreateForm = !showCreateForm">+ New Application</button>
      </div>

      <div *ngIf="showCreateForm" style="background:#f9f9f9; padding:16px; border-radius:6px; margin-bottom:16px">
        <h4>Create Application</h4>
        <p style="color:#888; margin-top:8px">Form UI coming in Phase 4.</p>
      </div>

      <div *ngIf="loading$ | async" style="text-align:center; padding:32px; color:#888">Loading applications...</div>

      <table *ngIf="!(loading$ | async)" style="width:100%; border-collapse:collapse">
        <thead>
          <tr style="border-bottom:2px solid #e0e0e0; text-align:left">
            <th style="padding:8px">Applicant</th>
            <th style="padding:8px">Loan Amount</th>
            <th style="padding:8px">Status</th>
            <th style="padding:8px">Date</th>
            <th style="padding:8px">Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let app of applications$ | async"
              style="border-bottom:1px solid #f0f0f0; cursor:pointer"
              [routerLink]="['/applications', app.id]">
            <td style="padding:8px">
              <div>{{ app.applicantName }}</div>
              <div style="color:#888; font-size:12px">{{ app.applicantEmail }}</div>
            </td>
            <td style="padding:8px">\${{ app.loanAmountRequested | number:'1.0-0' }}</td>
            <td style="padding:8px">
              <span class="badge badge-submitted">{{ app.status }}</span>
            </td>
            <td style="padding:8px">{{ app.createdAt | date:'MMM d, y' }}</td>
            <td style="padding:8px">
              <a [routerLink]="['/applications', app.id]" style="color:#1a237e">View</a>
            </td>
          </tr>
        </tbody>
      </table>

      <div *ngIf="(applications$ | async)?.length === 0 && !(loading$ | async)"
           style="text-align:center; padding:48px; color:#aaa">
        No applications yet. Create your first one above.
      </div>
    </div>
  `,
})
export class ApplicationListComponent implements OnInit {
  applications$: Observable<LoanApplication[]>;
  loading$: Observable<boolean>;
  showCreateForm = false;

  constructor(private store: Store<any>) {
    this.applications$ = this.store.select(state => state.applications.list);
    this.loading$ = this.store.select(state => state.applications.loading);
  }

  ngOnInit(): void {
    this.store.dispatch(loadApplications());
  }
}
