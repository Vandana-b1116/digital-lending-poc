package com.digitallending.document.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
@Profile("local")
@Slf4j
public class LocalStorageServiceImpl implements StorageService {

    @Value("${storage.local.base-path:/tmp/documents}")
    private String basePath;

    @Override
    public String store(Long applicationId, MultipartFile file) throws IOException {
        Path dir = Paths.get(basePath, applicationId.toString());
        Files.createDirectories(dir);

        String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path target = dir.resolve(filename);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        log.info("Stored {} for application {}", filename, applicationId);
        return target.toString();
    }

    @Override
    public Resource retrieve(String storagePath) throws IOException {
        Path path = Paths.get(storagePath);
        if (!Files.exists(path)) {
            throw new IOException("File not found at path: " + storagePath);
        }
        return new FileSystemResource(path);
    }
}
