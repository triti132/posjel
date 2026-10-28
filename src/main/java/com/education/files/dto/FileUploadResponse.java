package com.education.files.dto;

public record FileUploadResponse(
        Long id,
        String originalFilename,
        String mimeType,
        Long sizeBytes,
        String downloadUrl
) {
}
