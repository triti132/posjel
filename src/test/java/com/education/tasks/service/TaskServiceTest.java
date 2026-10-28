package com.education.tasks.service;

import com.education.common.exception.BusinessException;
import com.education.courses.entity.Course;
import com.education.courses.entity.Lesson;
import com.education.courses.repository.LessonRepository;
import com.education.courses.service.CourseAccessService;
import com.education.tasks.dto.TaskAttemptRequest;
import com.education.tasks.dto.TaskCreateRequest;
import com.education.tasks.entity.AttemptStatus;
import com.education.tasks.entity.Task;
import com.education.tasks.entity.TaskType;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskService Unit Tests")
class TaskServiceTest {

    @Mock private TaskRepository taskRepository;
    @Mock private TaskAttemptRepository taskAttemptRepository;
    @Mock private LessonRepository lessonRepository;
    @Mock private UserRepository userRepository;
    @Mock private CourseAccessService courseAccessService;

    @InjectMocks private TaskService taskService;

    private User teacher;
    private User student;
    private CustomUserDetails teacherDetails;
    private CustomUserDetails studentDetails;
    private Lesson lesson;

    @BeforeEach
    void setUp() {
        teacher = User.builder().id(1L).role(Role.TEACHER).build();
        student = User.builder().id(2L).role(Role.STUDENT).build();
        teacherDetails = new CustomUserDetails(teacher);
        studentDetails = new CustomUserDetails(student);

        Course course = Course.builder().id(10L).author(teacher).build();
        lesson = Lesson.builder().id(100L).course(course).isPublished(true).build();
    }

    @Test
    @DisplayName("Студент не может создать задание")
    void createTask_ByStudent_ShouldThrowForbidden() {
        TaskCreateRequest request = new TaskCreateRequest(TaskType.SHORT_TEXT, "Condition", "Answer");

        assertThatThrownBy(() -> taskService.createTask(100L, request, studentDetails))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Студенты не могут создавать задания");
    }

    @Test
    @DisplayName("Проверка короткого текста должна быть регистронезависимой и игнорировать лишние пробелы")
    void submitAttempt_ShortText_ShouldNormalizeAndCheck() {
        Task task = Task.builder()
                .id(1L)
                .lesson(lesson)
                .taskType(TaskType.SHORT_TEXT)
                .correctAnswer("  Spring Boot  ")
                .build();

        TaskAttemptRequest request = new TaskAttemptRequest("spring boot");

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(userRepository.findById(2L)).thenReturn(Optional.of(student));

        var response = taskService.submitAttempt(1L, request, studentDetails);

        assertThat(response.status()).isEqualTo(AttemptStatus.CORRECT);
        verify(taskAttemptRepository, times(1)).save(any());
    }
}
