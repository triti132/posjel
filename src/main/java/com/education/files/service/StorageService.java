package com.education.files.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    String storeFile(MultipartFile file, String storedFilename);
    Resource loadFileAsResource(String storedFilename);
    void deleteFile(String storedFilename);
}
