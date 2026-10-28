package com.education.progress.service;

import com.education.common.exception.BusinessException;
import com.education.courses.repository.CourseRepository;
import com.education.courses.repository.LessonRepository;
import com.education.courses.service.CourseAccessService;
import com.education.courses.entity.Course;
import com.education.courses.entity.Lesson;
import com.education.progress.entity.LessonProgress;
import com.education.progress.repository.LessonProgressRepository;
import com.education.tasks.repository.TaskAttemptRepository;
import com.education.tasks.repository.TaskRepository;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProgressService Unit Tests")
class ProgressServiceTest {

    @Mock private LessonProgressRepository lessonProgressRepository;
    @Mock private LessonRepository lessonRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private TaskRepository taskRepository;
    @Mock private TaskAttemptRepository taskAttemptRepository;
    @Mock private UserRepository userRepository;
    @Mock private CourseAccessService courseAccessService;

    @InjectMocks private ProgressService progressService;

    private User student;
    private CustomUserDetails studentDetails;

    @BeforeEach
    void setUp() {
        student = User.builder().id(2L).role(Role.STUDENT).build();
        studentDetails = new CustomUserDetails(student);
    }

    @Test
    @DisplayName("Нельзя завершить урок с заданиями, если не было ни одной попытки")
    void completeLesson_WithTasksButNoAttempts_ShouldThrowForbidden() {
        User teacher = User.builder().id(1L).role(Role.TEACHER).build();
        Course course = Course.builder().id(10L).author(teacher).build();
        Lesson lesson = Lesson.builder().id(100L).course(course).isPublished(true).build();
        when(lessonRepository.findById(100L)).thenReturn(Optional.of(lesson));
        when(taskRepository.countByLessonId(100L)).thenReturn(2L);
        when(taskAttemptRepository.existsByTask_Lesson_IdAndUser_Id(100L, 2L)).thenReturn(false);

        assertThatThrownBy(() -> progressService.completeLesson(100L, studentDetails))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("необходимо выполнить хотя бы одно задание");

        verify(lessonProgressRepository, never()).save(any());
    }
}
