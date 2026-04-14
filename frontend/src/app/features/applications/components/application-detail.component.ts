import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs';
import { LoanApplication } from '../../../store/applications/applications.model';
import { loadApplication } from '../../../store/applications/applications.actions';

@Component({
  selector: 'app-application-detail',
  template: `
    <div *ngIf="loading$ | async" style="text-align:center; padding:48px; color:#888">Loading...</div>

    <ng-container *ngIf="application$ | async as app">
      <div style="margin-bottom:16px">
        <a routerLink="/applications" style="color:#1a237e; text-decoration:none">← Back to Applications</a>
      </div>

      <div class="card" style="margin-bottom:16px">
        <div style="display:flex; justify-content:space-between; align-items:flex-start">
          <div>
            <h2>{{ app.applicantName }}</h2>
            <div style="color:#666; margin-top:4px">{{ app.applicantEmail }}</div>
          </div>
          <span class="badge badge-submitted">{{ app.status }}</span>
        </div>
        <hr style="margin:16px 0; border:none; border-top:1px solid #eee">
        <div style="display:grid; grid-template-columns:1fr 1fr; gap:16px">
          <div>
            <div style="color:#888; font-size:12px">LOAN AMOUNT</div>
            <div style="font-size:1.25rem; font-weight:600">\${{ app.loanAmountRequested | number:'1.0-0' }}</div>
          </div>
          <div>
            <div style="color:#888; font-size:12px">SELF-REPORTED INCOME</div>
            <div style="font-size:1.25rem; font-weight:600">\${{ app.selfReportedAnnualIncome | number:'1.0-0' }}/yr</div>
          </div>
          <div>
            <div style="color:#888; font-size:12px">EMPLOYER (SELF-REPORTED)</div>
            <div>{{ app.selfReportedEmployer }}</div>
          </div>
          <div>
            <div style="color:#888; font-size:12px">SUBMITTED</div>
            <div>{{ app.createdAt | date:'medium' }}</div>
          </div>
        </div>
      </div>

      <!-- Document upload section -->
      <div class="card">
        <h3 style="margin-bottom:16px">Documents</h3>
        <app-document-upload [applicationId]="app.id"></app-document-upload>
      </div>
    </ng-container>
  `,
})
export class ApplicationDetailComponent implements OnInit {
  application$: Observable<LoanApplication | null>;
  loading$: Observable<boolean>;

  constructor(private store: Store<any>, private route: ActivatedRoute) {
    this.application$ = this.store.select(state => state.applications.selected);
    this.loading$ = this.store.select(state => state.applications.loading);
  }

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.store.dispatch(loadApplication({ id }));
  }
}
