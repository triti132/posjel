package com.education.files.service;

import com.education.common.exception.BusinessException;
import com.education.courses.entity.Course;
import com.education.courses.entity.CourseStatus;
import com.education.courses.repository.CourseRepository;
import com.education.courses.service.CourseAccessService;
import com.education.files.dto.FileUploadResponse;
import com.education.files.entity.StoredFile;
import com.education.files.repository.StoredFileRepository;
import com.education.users.entity.Role;
import com.education.users.entity.User;
import com.education.users.repository.UserRepository;
import com.education.users.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FileService Unit Tests")
class FileServiceTest {

    @Mock
    private StorageService storageService;

    @Mock
    private StoredFileRepository storedFileRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private CourseAccessService courseAccessService;

    @InjectMocks
    private FileService fileService;

    private User teacherUser;
    private CustomUserDetails teacherDetails;

    @BeforeEach
    void setUp() {
        teacherUser = User.builder()
                .id(1L)
                .email("teacher@test.com")
                .role(Role.TEACHER)
                .build();

        teacherDetails = new CustomUserDetails(teacherUser);

        // Устанавливаем значения из конфигурации через reflection
        ReflectionTestUtils.setField(fileService, "allowedMimeTypes",
                "image/jpeg,image/png,application/pdf");
        ReflectionTestUtils.setField(fileService, "maxFileSizeBytes", 52428800L);
    }

    @Test
    @DisplayName("Загрузка файла с допустимым MIME-типом должна быть успешной")
    void uploadFile_WithValidMimeType_ShouldSucceed() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(teacherUser));
        when(storageService.storeFile(any(), anyString())).thenReturn("uuid.jpg");
        when(storedFileRepository.save(any(StoredFile.class))).thenAnswer(invocation -> {
            StoredFile storedFile = invocation.getArgument(0);
            storedFile.setId(100L);
            return storedFile;
        });

        FileUploadResponse response = fileService.uploadFile(file, null, teacherDetails);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.mimeType()).isEqualTo("image/jpeg");
        verify(storageService, times(1)).storeFile(eq(file), anyString());
    }

    @Test
    @DisplayName("Загрузка файла с недопустимым MIME-типом должна быть отклонена")
    void uploadFile_WithInvalidMimeType_ShouldThrowException() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.exe",
                "application/x-executable",
                "malicious content".getBytes()
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(teacherUser));

        assertThatThrownBy(() -> fileService.uploadFile(file, null, teacherDetails))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Недопустимый тип файла");

        verify(storageService, never()).storeFile(any(), anyString());
    }

    @Test
    @DisplayName("Загрузка файла превышающего максимальный размер должна быть отклонена")
    void uploadFile_ExceedingMaxSize_ShouldThrowException() {
        // Создаем файл размером больше лимита (50MB + 1 байт)
        byte[] largeContent = new byte[52428801];
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "large.jpg",
                "image/jpeg",
                largeContent
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(teacherUser));

        assertThatThrownBy(() -> fileService.uploadFile(file, null, teacherDetails))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Размер файла превышает допустимый лимит");

        verify(storageService, never()).storeFile(any(), anyString());
    }

    @Test
    @DisplayName("Загрузка файла в чужой курс должна быть запрещена")
    void uploadFile_ToOtherCourse_ShouldThrowForbidden() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test content".getBytes()
        );

        User otherTeacher = User.builder()
                .id(2L)
                .email("other@test.com")
                .role(Role.TEACHER)
                .build();

        Course course = Course.builder()
                .id(100L)
                .title("Other Course")
                .author(otherTeacher)
                .status(CourseStatus.DRAFT)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(teacherUser));
        when(courseRepository.findById(100L)).thenReturn(Optional.of(course));
        doThrow(new BusinessException("FORBIDDEN", "Нет прав на загрузку файлов в этот курс"))
                .when(courseAccessService).checkEditAccess(100L, teacherDetails);

        assertThatThrownBy(() -> fileService.uploadFile(file, 100L, teacherDetails))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Нет прав на загрузку файлов в этот курс");

        verify(storageService, never()).storeFile(any(), anyString());
    }

    @Test
    @DisplayName("Загрузка файла в свой курс должна быть успешной")
    void uploadFile_ToOwnCourse_ShouldSucceed() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test content".getBytes()
        );

        Course course = Course.builder()
                .id(100L)
                .title("My Course")
                .author(teacherUser)
                .status(CourseStatus.DRAFT)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(teacherUser));
        when(courseRepository.findById(100L)).thenReturn(Optional.of(course));
        when(storageService.storeFile(any(), anyString())).thenReturn("uuid.jpg");
        when(storedFileRepository.save(any(StoredFile.class))).thenAnswer(invocation -> {
            StoredFile storedFile = invocation.getArgument(0);
            storedFile.setId(100L);
            return storedFile;
        });

        FileUploadResponse response = fileService.uploadFile(file, 100L, teacherDetails);

        assertThat(response).isNotNull();
        verify(storageService, times(1)).storeFile(eq(file), anyString());
    }
}
