import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, timer } from 'rxjs';
import { switchMap, takeWhile } from 'rxjs/operators';
import { ExtractionResult, FieldApproval } from '../../store/extractions/extractions.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ExtractionService {
  private readonly base = `${environment.llmExtractionServiceUrl}/api/extractions`;

  constructor(private http: HttpClient) {}

  getExtractionByDocument(documentId: number): Observable<ExtractionResult> {
    return this.http.get<ExtractionResult>(`${this.base}/document/${documentId}`);
  }

  /** Polls every 3 seconds until extraction status is PENDING_REVIEW (complete) */
  pollUntilComplete(documentId: number): Observable<ExtractionResult> {
    return timer(0, 3000).pipe(
      switchMap(() => this.getExtractionByDocument(documentId)),
      takeWhile(result => result.status === 'PENDING_REVIEW', true)
    );
  }

  submitFieldReview(extractionId: number, approvals: FieldApproval[]): Observable<void> {
    return this.http.post<void>(`${this.base}/${extractionId}/review`, { approvals });
  }

  approveExtraction(extractionId: number): Observable<void> {
    return this.http.post<void>(`${this.base}/${extractionId}/approve`, {});
  }
}
