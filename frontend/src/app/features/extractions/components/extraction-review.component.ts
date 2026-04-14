import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs';
import { ExtractionResult, FieldApproval, OfficerAction } from '../../../store/extractions/extractions.model';
import { loadExtraction, approveExtraction } from '../../../store/extractions/extractions.actions';
import { ExtractionService } from '../../../core/services/extraction.service';

interface FieldDecision {
  fieldName: string;
  displayLabel: string;
  extractedValue: string | null;
  action: OfficerAction | null;
  editedValue: string;
  editing: boolean;
  notes: string;
}

@Component({
  selector: 'app-extraction-review',
  template: `
    <div style="margin-bottom:16px">
      <a [routerLink]="['/applications', applicationId]" style="color:#1a237e; text-decoration:none">← Back to Application</a>
    </div>

    <div *ngIf="loading$ | async" style="text-align:center; padding:48px; color:#888">Loading extraction results...</div>

    <ng-container *ngIf="extraction$ | async as ext">
      <!-- Discrepancy banner -->
      <div *ngIf="ext.discrepancyFlags?.length"
           style="background:#fce4ec; border:1px solid #e57373; border-radius:6px; padding:12px 16px; margin-bottom:16px; color:#c62828">
        <strong>{{ ext.discrepancyFlags.length }} discrepancy flag(s) detected.</strong>
        Review highlighted fields before approving.
      </div>

      <div style="display:grid; grid-template-columns:1fr 1fr; gap:16px; margin-bottom:16px">
        <!-- Left: AI-extracted data -->
        <div class="card">
          <h3 style="margin-bottom:16px">AI-Extracted Data
            <span style="font-size:12px; font-weight:400; color:#888; margin-left:8px">
              Confidence: {{ (ext.confidenceScore ?? 0) * 100 | number:'1.0-0' }}%
            </span>
          </h3>

          <div *ngFor="let field of fieldDecisions" style="margin-bottom:12px; padding-bottom:12px; border-bottom:1px solid #f0f0f0">
            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:4px">
              <span style="font-weight:500; font-size:13px">{{ field.displayLabel }}</span>
              <span *ngIf="hasDiscrepancy(ext, field.fieldName)"
                    style="font-size:11px; background:#fce4ec; color:#c62828; padding:2px 8px; border-radius:10px">
                ⚠ Discrepancy
              </span>
            </div>

            <div *ngIf="!field.editing" style="margin-bottom:6px; color:#333">
              {{ field.extractedValue ?? '—' }}
            </div>
            <input *ngIf="field.editing" [(ngModel)]="field.editedValue"
                   style="width:100%; padding:6px; border:1px solid #90caf9; border-radius:4px; margin-bottom:6px">

            <div style="display:flex; gap:6px">
              <button class="btn btn-success" style="font-size:11px; padding:4px 10px"
                      [disabled]="field.action === 'ACCEPTED'"
                      (click)="accept(field)">Accept</button>
              <button class="btn btn-warning" style="font-size:11px; padding:4px 10px"
                      (click)="toggleEdit(field)">Edit</button>
              <button class="btn btn-danger" style="font-size:11px; padding:4px 10px"
                      [disabled]="field.action === 'REJECTED'"
                      (click)="reject(field)">Reject</button>
            </div>
            <div *ngIf="field.action" style="font-size:11px; margin-top:4px"
                 [style.color]="field.action === 'ACCEPTED' ? '#2e7d32' : field.action === 'REJECTED' ? '#c62828' : '#e65100'">
              ✓ {{ field.action }}
            </div>
          </div>
        </div>

        <!-- Right: Self-reported data -->
        <div class="card">
          <h3 style="margin-bottom:16px">Applicant Self-Reported</h3>
          <div style="color:#888; font-size:13px">
            <p>Employer: <strong>{{ selfReportedEmployer }}</strong></p>
            <p style="margin-top:8px">Annual Income: <strong>\${{ selfReportedIncome | number:'1.0-0' }}</strong></p>
          </div>
          <hr style="margin:16px 0; border:none; border-top:1px solid #eee">
          <h4 style="margin-bottom:12px; font-size:13px">Discrepancy Details</h4>
          <div *ngFor="let flag of ext.discrepancyFlags" style="margin-bottom:10px; font-size:12px">
            <span [class]="'flag-' + flag.severity.toLowerCase()">● {{ flag.severity }}</span>
            <strong style="margin-left:4px">{{ flag.fieldName }}</strong><br>
            <span style="color:#666">
              Extracted: {{ flag.extractedValue }} |
              Reported: {{ flag.selfReportedValue }}
              <span *ngIf="flag.variancePercentage"> ({{ flag.variancePercentage | number:'1.1-1' }}% diff)</span>
            </span>
          </div>
          <div *ngIf="!ext.discrepancyFlags?.length" style="color:#aaa; font-size:13px">No discrepancies detected.</div>
        </div>
      </div>

      <!-- Summary bar -->
      <div class="card" style="display:flex; align-items:center; gap:24px">
        <div style="color:#2e7d32">✓ Accepted: {{ countByAction('ACCEPTED') }}</div>
        <div style="color:#e65100">✎ Edited: {{ countByAction('EDITED') }}</div>
        <div style="color:#c62828">✗ Rejected: {{ countByAction('REJECTED') }}</div>
        <div style="flex:1"></div>
        <button class="btn btn-primary"
                [disabled]="!allFlaggedFieldsActioned(ext) || (approving$ | async)"
                (click)="submitApproval(ext.id)">
          {{ (approving$ | async) ? 'Sending...' : 'Approve & Send to Underwriting' }}
        </button>
      </div>
    </ng-container>
  `,
})
export class ExtractionReviewComponent implements OnInit {
  extraction$: Observable<ExtractionResult | null>;
  loading$: Observable<boolean>;
  approving$: Observable<boolean>;
  applicationId!: number;
  documentId!: number;
  fieldDecisions: FieldDecision[] = [];
  selfReportedEmployer = '';
  selfReportedIncome = 0;

  private readonly fieldConfig: { key: keyof ExtractionResult; label: string }[] = [
    { key: 'employerName', label: 'Employer Name' },
    { key: 'payPeriodStart', label: 'Pay Period Start' },
    { key: 'payPeriodEnd', label: 'Pay Period End' },
    { key: 'grossPay', label: 'Gross Pay' },
    { key: 'netPay', label: 'Net Pay' },
    { key: 'ytdGross', label: 'YTD Gross' },
    { key: 'ytdNet', label: 'YTD Net' },
    { key: 'payFrequency', label: 'Pay Frequency' },
  ];

  constructor(
    private store: Store<any>,
    private route: ActivatedRoute,
    private router: Router,
    private extractionService: ExtractionService
  ) {
    this.extraction$ = this.store.select(state => state.extractions.current);
    this.loading$ = this.store.select(state => state.extractions.loading);
    this.approving$ = this.store.select(state => state.extractions.approving);
  }

  ngOnInit(): void {
    this.applicationId = Number(this.route.snapshot.paramMap.get('id'));
    this.documentId = Number(this.route.snapshot.paramMap.get('documentId'));
    this.store.dispatch(loadExtraction({ documentId: this.documentId }));

    this.extraction$.subscribe(ext => {
      if (ext && this.fieldDecisions.length === 0) {
        this.fieldDecisions = this.fieldConfig.map(f => ({
          fieldName: f.key,
          displayLabel: f.label,
          extractedValue: ext[f.key] != null ? String(ext[f.key]) : null,
          action: null,
          editedValue: '',
          editing: false,
          notes: '',
        }));
      }
    });
  }

  hasDiscrepancy(ext: ExtractionResult, fieldName: string): boolean {
    return ext.discrepancyFlags?.some(f => f.fieldName === fieldName) ?? false;
  }

  accept(field: FieldDecision): void {
    field.action = 'ACCEPTED';
    field.editing = false;
  }

  toggleEdit(field: FieldDecision): void {
    field.editing = !field.editing;
    if (field.editing) field.editedValue = field.extractedValue ?? '';
    else if (field.editedValue) field.action = 'EDITED';
  }

  reject(field: FieldDecision): void {
    field.action = 'REJECTED';
    field.editing = false;
  }

  countByAction(action: OfficerAction): number {
    return this.fieldDecisions.filter(f => f.action === action).length;
  }

  allFlaggedFieldsActioned(ext: ExtractionResult): boolean {
    if (!ext.discrepancyFlags?.length) return this.fieldDecisions.every(f => f.action !== null);
    const flaggedFields = ext.discrepancyFlags.map(f => f.fieldName);
    return flaggedFields.every(fn => this.fieldDecisions.find(fd => fd.fieldName === fn)?.action != null);
  }

  submitApproval(extractionId: number): void {
    const approvals: FieldApproval[] = this.fieldDecisions
      .filter(f => f.action !== null)
      .map(f => ({
        fieldName: f.fieldName,
        finalValue: f.action === 'EDITED' ? f.editedValue : (f.extractedValue ?? ''),
        officerAction: f.action!,
        officerNotes: f.notes,
      }));

    this.extractionService.submitFieldReview(extractionId, approvals).subscribe(() => {
      this.store.dispatch(approveExtraction({ extractionId }));
      this.router.navigate(['/applications', this.applicationId]);
    });
  }
}
