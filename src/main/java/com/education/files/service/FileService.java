package com.education.files.service;

import com.education.common.exception.BusinessException;
import com.education.courses.entity.Course;
import com.education.courses.repository.CourseRepository;
import com.education.courses.service.CourseAccessService;
import com.education.files.dto.FileUploadResponse;
import com.education.files.entity.StoredFile;
import com.education.files.repository.StoredFileRepository;
import com.education.users.entity.User;
import com.education.users.repository.UserRepository;
import com.education.users.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {

    private final StorageService storageService;
    private final StoredFileRepository storedFileRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final CourseAccessService courseAccessService;

    @Value("${app.storage.allowed-mime-types}")
    private String allowedMimeTypes;

    @Value("${app.storage.max-file-size-bytes}")
    private long maxFileSizeBytes;

    @Transactional
    public FileUploadResponse uploadFile(MultipartFile file, Long courseId, CustomUserDetails userDetails) {
        User uploader = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Пользователь не найден"));

        // 1. Проверка MIME-типа (п. 6.3 ТЗ)
        List<String> allowedTypes = Arrays.asList(allowedMimeTypes.split(","));
        String contentType = file.getContentType();
        if (contentType == null || !allowedTypes.contains(contentType)) {
            throw new BusinessException("INVALID_FILE_TYPE", "Недопустимый тип файла. Разрешены: " + allowedMimeTypes);
        }

        // 2. Проверка размера (п. 6.3 ТЗ)
        if (file.getSize() > maxFileSizeBytes) {
            throw new BusinessException("FILE_TOO_LARGE", "Размер файла превышает допустимый лимит");
        }

        // 3. Проверка прав на курс (если файл привязывается к курсу)
        Course course = null;
        if (courseId != null) {
            course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new BusinessException("COURSE_NOT_FOUND", "Курс не найден"));

            courseAccessService.checkEditAccess(courseId, userDetails);
        }

        // 4. Генерация безопасного имени файла (п. 6.3 ТЗ)
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
        String storedFileName = UUID.randomUUID().toString() + extension;

        // 5. Сохранение в хранилище и БД
        storageService.storeFile(file, storedFileName);

        StoredFile storedFile = StoredFile.builder()
                .originalFilename(originalFilename != null ? originalFilename : "unknown")
                .storedFilename(storedFileName)
                .mimeType(contentType)
                .sizeBytes(file.getSize())
                .course(course)
                .uploadedBy(uploader)
                .build();

        StoredFile savedFile = storedFileRepository.save(storedFile);

        return new FileUploadResponse(
                savedFile.getId(),
                savedFile.getOriginalFilename(),
                savedFile.getMimeType(),
                savedFile.getSizeBytes(),
                "/api/v1/files/" + savedFile.getId() + "/download"
        );
    }

    @Transactional(readOnly = true)
    public Resource getFileForDownload(Long fileId, CustomUserDetails userDetails) {
        StoredFile file = storedFileRepository.findById(fileId)
                .orElseThrow(() -> new BusinessException("FILE_NOT_FOUND", "Файл не найден"));

        // Проверка прав доступа перед скачиванием (п. 6.3 и 11.4 ТЗ)
        boolean isOwner = file.getUploadedBy().getId().equals(userDetails.getId());
        boolean isAdmin = "ADMIN".equals(userDetails.getRole());

        boolean hasCourseAccess = file.getCourse() != null
                && courseAccessService.hasAccess(file.getCourse(), userDetails);

        if (!isOwner && !isAdmin && !hasCourseAccess) {
            throw new BusinessException("FORBIDDEN", "Нет прав на скачивание этого файла");
        }

        return storageService.loadFileAsResource(file.getStoredFilename());
    }
}
