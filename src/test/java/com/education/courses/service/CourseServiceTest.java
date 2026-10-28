package com.education.courses.service;

import com.education.common.exception.BusinessException;
import com.education.courses.dto.CourseCreateRequest;
import com.education.courses.dto.CourseResponse;
import com.education.courses.dto.CourseUpdateRequest;
import com.education.courses.entity.Course;
import com.education.courses.entity.CourseStatus;
import com.education.courses.entity.CourseVisibility;
import com.education.courses.repository.CourseRepository;
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

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CourseService Unit Tests")
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CourseAccessService courseAccessService;

    @InjectMocks
    private CourseService courseService;

    private User teacherUser;
    private User studentUser;
    private User adminUser;
    private CustomUserDetails teacherDetails;
    private CustomUserDetails studentDetails;
    private CustomUserDetails adminDetails;

    @BeforeEach
    void setUp() {
        teacherUser = User.builder()
                .id(1L)
                .email("teacher@test.com")
                .fullName("Teacher User")
                .role(Role.TEACHER)
                .build();

        studentUser = User.builder()
                .id(2L)
                .email("student@test.com")
                .fullName("Student User")
                .role(Role.STUDENT)
                .build();

        adminUser = User.builder()
                .id(3L)
                .email("admin@test.com")
                .fullName("Admin User")
                .role(Role.ADMIN)
                .build();

        teacherDetails = new CustomUserDetails(teacherUser);
        studentDetails = new CustomUserDetails(studentUser);
        adminDetails = new CustomUserDetails(adminUser);
    }

    @Test
    @DisplayName("Создание курса преподавателем должно быть успешным")
    void createCourse_ByTeacher_ShouldSucceed() {
        CourseCreateRequest request = new CourseCreateRequest(
                "Test Course",
                "Description",
                "cover.jpg",
                "Mathematics",
                CourseVisibility.PUBLIC
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(teacherUser));
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> {
            Course course = invocation.getArgument(0);
            course.setId(100L);
            course.setCreatedAt(LocalDateTime.now());
            return course;
        });

        CourseResponse response = courseService.createCourse(request, 1L);

        assertThat(response).isNotNull();
        assertThat(response.title()).isEqualTo("Test Course");
        assertThat(response.status()).isEqualTo(CourseStatus.DRAFT);
        verify(courseRepository, times(1)).save(any(Course.class));
    }

    @Test
    @DisplayName("Создание курса студентом должно быть запрещено")
    void createCourse_ByStudent_ShouldThrowForbidden() {
        CourseCreateRequest request = new CourseCreateRequest(
                "Test Course",
                "Description",
                "cover.jpg",
                "Mathematics",
                CourseVisibility.PUBLIC
        );

        when(userRepository.findById(2L)).thenReturn(Optional.of(studentUser));

        assertThatThrownBy(() -> courseService.createCourse(request, 2L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Только преподаватели и администраторы могут создавать курсы");

        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    @DisplayName("Редактирование курса автором должно быть успешным")
    void updateCourse_ByAuthor_ShouldSucceed() {
        Course course = Course.builder()
                .id(100L)
                .title("Old Title")
                .description("Old Description")
                .author(teacherUser)
                .status(CourseStatus.DRAFT)
                .build();

        CourseUpdateRequest request = new CourseUpdateRequest(
                "New Title",
                "New Description",
                "new-cover.jpg",
                "Physics",
                CourseVisibility.PUBLIC
        );

        when(courseRepository.findById(100L)).thenReturn(Optional.of(course));
        when(courseRepository.save(any(Course.class))).thenReturn(course);

        CourseResponse response = courseService.updateCourse(100L, request, teacherDetails);

        assertThat(response).isNotNull();
        verify(courseRepository, times(1)).save(course);
    }

    @Test
    @DisplayName("Редактирование курса не автором должно быть запрещено")
    void updateCourse_ByNonAuthor_ShouldThrowForbidden() {
        Course course = Course.builder()
                .id(100L)
                .title("Old Title")
                .author(teacherUser)
                .build();

        CourseUpdateRequest request = new CourseUpdateRequest(
                "New Title",
                "New Description",
                "new-cover.jpg",
                "Physics",
                CourseVisibility.PUBLIC
        );

        when(courseRepository.findById(100L)).thenReturn(Optional.of(course));
        doThrow(new BusinessException("FORBIDDEN", "Только автор курса или администратор может редактировать"))
                .when(courseAccessService).checkEditAccess(100L, studentDetails);

        assertThatThrownBy(() -> courseService.updateCourse(100L, request, studentDetails))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Только автор курса или администратор может редактировать");

        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    @DisplayName("Редактирование курса администратором должно быть успешным")
    void updateCourse_ByAdmin_ShouldSucceed() {
        Course course = Course.builder()
                .id(100L)
                .title("Old Title")
                .author(teacherUser)
                .build();

        CourseUpdateRequest request = new CourseUpdateRequest(
                "New Title",
                "New Description",
                "new-cover.jpg",
                "Physics",
                CourseVisibility.PUBLIC
        );

        when(courseRepository.findById(100L)).thenReturn(Optional.of(course));
        when(courseRepository.save(any(Course.class))).thenReturn(course);

        CourseResponse response = courseService.updateCourse(100L, request, adminDetails);

        assertThat(response).isNotNull();
        verify(courseRepository, times(1)).save(course);
    }

    @Test
    @DisplayName("Публикация курса автором должна быть успешной")
    void publishCourse_ByAuthor_ShouldSucceed() {
        Course course = Course.builder()
                .id(100L)
                .title("Test Course")
                .author(teacherUser)
                .status(CourseStatus.DRAFT)
                .build();

        when(courseRepository.findById(100L)).thenReturn(Optional.of(course));
        when(courseRepository.save(any(Course.class))).thenReturn(course);

        CourseResponse response = courseService.publishCourse(100L, teacherDetails);

        assertThat(response).isNotNull();
        assertThat(course.getStatus()).isEqualTo(CourseStatus.PUBLISHED);
    }

    @Test
    @DisplayName("Копирование курса должно создать независимую копию с новым автором")
    void copyCourse_ShouldCreateIndependentCopy() {
        Course original = Course.builder()
                .id(100L)
                .title("Original Course")
                .description("Original Description")
                .author(teacherUser)
                .status(CourseStatus.PUBLISHED)
                .build();

        User newAuthor = User.builder()
                .id(4L)
                .email("newteacher@test.com")
                .role(Role.TEACHER)
                .build();

        when(courseRepository.findById(100L)).thenReturn(Optional.of(original));
        CustomUserDetails newAuthorDetails = new CustomUserDetails(newAuthor);
        when(userRepository.findById(4L)).thenReturn(Optional.of(newAuthor));
        when(courseAccessService.hasAccess(original, newAuthorDetails)).thenReturn(true);
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> {
            Course copy = invocation.getArgument(0);
            copy.setId(200L);
            return copy;
        });

        CourseResponse response = courseService.copyCourse(100L, newAuthorDetails);

        assertThat(response).isNotNull();
        assertThat(response.title()).isEqualTo("Original Course (Копия)");
        assertThat(response.status()).isEqualTo(CourseStatus.DRAFT);
        assertThat(response.authorId()).isEqualTo(4L);

        // Проверяем, что оригинал не был изменен
        assertThat(original.getStatus()).isEqualTo(CourseStatus.PUBLISHED);
        assertThat(original.getAuthor().getId()).isEqualTo(1L);
    }
}
