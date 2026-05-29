package com.digitallending.llmextraction.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service
@Slf4j
public class TextExtractionService {

    public String extractText(InputStream fileStream, String mimeType) throws IOException {
        if (mimeType != null && mimeType.contains("pdf")) {
            return extractFromPdf(fileStream);
        }
        throw new UnsupportedOperationException("Unsupported file type: " + mimeType);
    }

    private String extractFromPdf(InputStream fileStream) throws IOException {
        try (PDDocument document = Loader.loadPDF(fileStream.readAllBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            log.info("Extracted {} characters from PDF ({} pages)", text.length(), document.getNumberOfPages());
            return text;
        }
    }
}
