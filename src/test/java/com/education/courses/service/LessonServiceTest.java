package com.education.courses.service;

import com.education.common.exception.BusinessException;
import com.education.courses.dto.LessonCreateRequest;
import com.education.courses.dto.LessonResponse;
import com.education.courses.dto.LessonUpdateRequest;
import com.education.courses.entity.Course;
import com.education.courses.entity.Lesson;
import com.education.courses.repository.CourseRepository;
import com.education.courses.repository.LessonRepository;
import com.education.users.entity.Role;
import com.education.users.entity.User;
import com.education.users.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LessonService Unit Tests")
class LessonServiceTest {

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private CourseAccessService courseAccessService;

    @InjectMocks
    private LessonService lessonService;

    private User teacherUser;
    private User studentUser;
    private CustomUserDetails teacherDetails;
    private CustomUserDetails studentDetails;
    private Course course;

    @BeforeEach
    void setUp() {
        teacherUser = User.builder()
                .id(1L)
                .email("teacher@test.com")
                .role(Role.TEACHER)
                .build();

        studentUser = User.builder()
                .id(2L)
                .email("student@test.com")
                .role(Role.STUDENT)
                .build();

        teacherDetails = new CustomUserDetails(teacherUser);
        studentDetails = new CustomUserDetails(studentUser);

        course = Course.builder()
                .id(100L)
                .title("Test Course")
                .author(teacherUser)
                .build();
    }

    @Test
    @DisplayName("Создание урока автором курса должно быть успешным")
    void createLesson_ByCourseAuthor_ShouldSucceed() {
        LessonCreateRequest request = LessonCreateRequest.builder()
                .title("Test Lesson")
                .description("Description")
                .orderIndex(1)
                .build();

        when(courseRepository.findById(100L)).thenReturn(Optional.of(course));
        when(lessonRepository.save(any(Lesson.class))).thenAnswer(invocation -> {
            Lesson lesson = invocation.getArgument(0);
            lesson.setId(200L);
            lesson.setBlocks(new ArrayList<>()); // <-- ДОБАВИТЬ ЭТУ СТРОКУ
            return lesson;
        });

        LessonResponse response = lessonService.createLesson(100L, request, teacherDetails);

        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Test Lesson");
        verify(lessonRepository, times(1)).save(any(Lesson.class));
    }

    @Test
    @DisplayName("Создание урока не автором курса должно быть запрещено")
    void createLesson_ByNonAuthor_ShouldThrowForbidden() {
        LessonCreateRequest request = LessonCreateRequest.builder()
                .title("Test Lesson")
                .description("Description")
                .orderIndex(1)
                .build();

        when(courseRepository.findById(100L)).thenReturn(Optional.of(course));

        assertThatThrownBy(() -> lessonService.createLesson(100L, request, studentDetails))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("У вас нет прав на изменение этого курса");

        verify(lessonRepository, never()).save(any(Lesson.class));
    }

    @Test
    @DisplayName("Обновление урока автором курса должно быть успешным")
    void updateLesson_ByCourseAuthor_ShouldSucceed() {
        Lesson lesson = Lesson.builder()
                .id(200L)
                .course(course)
                .title("Old Title")
                .orderIndex(1)
                .blocks(new ArrayList<>()) // <-- ДОБАВИТЬ ЭТУ СТРОКУ
                .build();

        LessonUpdateRequest request = LessonUpdateRequest.builder()
                .title("New Title")
                .orderIndex(2)
                .build();

        when(lessonRepository.findById(200L)).thenReturn(Optional.of(lesson));
        when(lessonRepository.save(any(Lesson.class))).thenReturn(lesson);

        LessonResponse response = lessonService.updateLesson(200L, request, teacherDetails);

        assertThat(response).isNotNull();
        verify(lessonRepository, times(1)).save(lesson);
    }

    @Test
    @DisplayName("Удаление урока не автором курса должно быть запрещено")
    void deleteLesson_ByNonAuthor_ShouldThrowForbidden() {
        Lesson lesson = Lesson.builder()
                .id(200L)
                .course(course)
                .build();

        when(lessonRepository.findById(200L)).thenReturn(Optional.of(lesson));

        assertThatThrownBy(() -> lessonService.deleteLesson(200L, studentDetails))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("У вас нет прав на изменение этого курса");

        verify(lessonRepository, never()).delete(any(Lesson.class));
    }
}
