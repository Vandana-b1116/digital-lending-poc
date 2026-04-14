import { Component, Input } from '@angular/core';
import { Router } from '@angular/router';
import { DocumentService } from '../../../core/services/document.service';
import { ExtractionService } from '../../../core/services/extraction.service';

@Component({
  selector: 'app-document-upload',
  template: `
    <div
      class="drop-zone"
      [class.drag-over]="isDragging"
      (dragover)="onDragOver($event)"
      (dragleave)="isDragging = false"
      (drop)="onDrop($event)"
      (click)="fileInput.click()"
    >
      <input #fileInput type="file" accept=".pdf" hidden (change)="onFileSelected($event)">

      <div *ngIf="!uploading && !extracting">
        <div style="font-size:2rem; margin-bottom:8px">📄</div>
        <div style="font-weight:500">Drop pay stub PDF here or click to browse</div>
        <div style="color:#aaa; font-size:12px; margin-top:4px">Supports PDF, max 20MB</div>
      </div>

      <div *ngIf="uploading" style="text-align:center">
        <div class="spinner"></div>
        <div style="margin-top:8px; color:#1a237e">Uploading document...</div>
      </div>

      <div *ngIf="extracting" style="text-align:center">
        <div class="spinner"></div>
        <div style="margin-top:8px; color:#1a237e">AI extraction in progress...</div>
        <div style="color:#888; font-size:12px; margin-top:4px">This may take a few seconds</div>
      </div>
    </div>

    <div *ngIf="errorMessage" style="color:#c62828; margin-top:8px; font-size:13px">{{ errorMessage }}</div>
  `,
  styles: [`
    .drop-zone {
      border: 2px dashed #90caf9;
      border-radius: 8px;
      padding: 40px;
      text-align: center;
      cursor: pointer;
      transition: background .2s;
    }
    .drop-zone:hover, .drop-zone.drag-over { background: #e3f2fd; }
    .spinner {
      width: 32px; height: 32px;
      border: 3px solid #e3f2fd;
      border-top-color: #1a237e;
      border-radius: 50%;
      animation: spin 0.8s linear infinite;
      margin: 0 auto;
    }
    @keyframes spin { to { transform: rotate(360deg); } }
  `],
})
export class DocumentUploadComponent {
  @Input() applicationId!: number;
  isDragging = false;
  uploading = false;
  extracting = false;
  errorMessage = '';

  constructor(
    private documentService: DocumentService,
    private extractionService: ExtractionService,
    private router: Router
  ) {}

  onDragOver(event: DragEvent): void {
    event.preventDefault();
    this.isDragging = true;
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    this.isDragging = false;
    const file = event.dataTransfer?.files[0];
    if (file) this.upload(file);
  }

  onFileSelected(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (file) this.upload(file);
  }

  private upload(file: File): void {
    this.uploading = true;
    this.errorMessage = '';

    this.documentService.uploadDocument(this.applicationId, file).subscribe({
      next: (doc) => {
        this.uploading = false;
        this.extracting = true;
        this.pollExtraction(doc.id);
      },
      error: (err) => {
        this.uploading = false;
        this.errorMessage = 'Upload failed: ' + err.message;
      },
    });
  }

  private pollExtraction(documentId: number): void {
    this.extractionService.pollUntilComplete(documentId).subscribe({
      next: (result) => {
        if (result.status === 'PENDING_REVIEW') {
          this.extracting = false;
          this.router.navigate(['/applications', this.applicationId, 'review', documentId]);
        }
      },
      error: (err) => {
        this.extracting = false;
        this.errorMessage = 'Extraction polling failed: ' + err.message;
      },
    });
  }
}
