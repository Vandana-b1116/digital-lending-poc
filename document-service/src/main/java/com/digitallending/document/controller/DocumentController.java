package com.digitallending.document.controller;

import com.digitallending.document.dto.DocumentResponse;
import com.digitallending.document.entity.Document;
import com.digitallending.document.model.ExtractionStatus;
import com.digitallending.document.repository.DocumentRepository;
import com.digitallending.document.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class DocumentController {

    private final DocumentRepository documentRepository;
    private final StorageService storageService;

    @PostMapping("/upload")
    public ResponseEntity<DocumentResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("applicationId") Long applicationId,
            @RequestParam(value = "uploadedBy", required = false) String uploadedBy) {
        try {
            String storagePath = storageService.store(applicationId, file);

            Document doc = new Document();
            doc.setApplicationId(applicationId);
            doc.setOriginalFilename(file.getOriginalFilename());
            doc.setStoragePath(storagePath);
            doc.setMimeType(file.getContentType());
            doc.setFileSizeBytes(file.getSize());
            doc.setUploadedBy(uploadedBy);
            doc.setExtractionStatus(ExtractionStatus.PENDING);

            Document saved = documentRepository.save(doc);
            log.info("Uploaded document {} for application {}", saved.getId(), applicationId);
            return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
        } catch (Exception e) {
            log.error("Upload failed for application {}", applicationId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/application/{applicationId}")
    public List<DocumentResponse> getByApplication(@PathVariable Long applicationId) {
        return documentRepository.findByApplicationId(applicationId)
                .stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponse> getById(@PathVariable Long id) {
        try {
            Document doc = documentRepository.findById(id)
                    .orElseThrow(() -> new NoSuchElementException("Document not found: " + id));
            return ResponseEntity.ok(toResponse(doc));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/extraction-status")
    public ResponseEntity<DocumentResponse> updateExtractionStatus(
            @PathVariable Long id,
            @RequestParam ExtractionStatus status) {
        try {
            Document doc = documentRepository.findById(id)
                    .orElseThrow(() -> new NoSuchElementException("Document not found: " + id));
            doc.setExtractionStatus(status);
            return ResponseEntity.ok(toResponse(documentRepository.save(doc)));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private DocumentResponse toResponse(Document doc) {
        DocumentResponse r = new DocumentResponse();
        r.setId(doc.getId());
        r.setApplicationId(doc.getApplicationId());
        r.setDocumentType(doc.getDocumentType());
        r.setOriginalFilename(doc.getOriginalFilename());
        r.setMimeType(doc.getMimeType());
        r.setFileSizeBytes(doc.getFileSizeBytes());
        r.setUploadedBy(doc.getUploadedBy());
        r.setUploadedAt(doc.getUploadedAt());
        r.setExtractionStatus(doc.getExtractionStatus());
        return r;
    }
}
