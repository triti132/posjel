package com.education.files.service;

import com.education.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class LocalStorageServiceImpl implements StorageService {

    private final Path fileStorageLocation;

    public LocalStorageServiceImpl(@Value("${app.storage.upload-dir}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException ex) {
            throw new BusinessException("STORAGE_ERROR", "Не удалось создать директорию для загрузки файлов");
        }
    }

    @Override
    public String storeFile(MultipartFile file, String storedFilename) {
        try {
            if (file.isEmpty()) {
                throw new BusinessException("INVALID_FILE", "Файл пуст");
            }

            // защита от Directory Traversal
            Path targetLocation = this.fileStorageLocation.resolve(storedFilename).normalize();
            if (!targetLocation.startsWith(this.fileStorageLocation)) {
                throw new BusinessException("SECURITY_ERROR", "Небезопасное имя файла");
            }

            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return storedFilename;
        } catch (IOException ex) {
            throw new BusinessException("STORAGE_ERROR", "Не удалось сохранить файл " + ex.getMessage());
        }
    }

    @Override
    public Resource loadFileAsResource(String storedFileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(storedFileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new BusinessException("FILE_NOT_FOUND", "Файл не найден или не доступен для чтения");
            }
        } catch (Exception ex) {
            throw new BusinessException("FILE_NOT_FOUND", "Файл не найден: " + ex.getMessage());
        }
    }

    @Override
    public void deleteFile(String storedFileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(storedFileName).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new BusinessException("STORAGE_ERROR", "Не удалось удалить файл: " + ex.getMessage());
        }
    }
}
