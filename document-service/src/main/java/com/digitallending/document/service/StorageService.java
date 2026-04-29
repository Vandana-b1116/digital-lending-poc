package com.digitallending.document.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface StorageService {
    String store(Long applicationId, MultipartFile file) throws IOException;
}
