import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface DocumentMetadata {
  id: number;
  applicationId: number;
  documentType: string;
  originalFilename: string;
  extractionStatus: string;
  uploadedAt: string;
}

@Injectable({ providedIn: 'root' })
export class DocumentService {
  private readonly base = `${environment.documentServiceUrl}/api/documents`;

  constructor(private http: HttpClient) {}

  uploadDocument(applicationId: number, file: File): Observable<DocumentMetadata> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('applicationId', applicationId.toString());
    return this.http.post<DocumentMetadata>(`${this.base}/upload`, formData);
  }

  getDocument(id: number): Observable<DocumentMetadata> {
    return this.http.get<DocumentMetadata>(`${this.base}/${id}`);
  }

  getDocumentsForApplication(applicationId: number): Observable<DocumentMetadata[]> {
    return this.http.get<DocumentMetadata[]>(`${this.base}/application/${applicationId}`);
  }
}
