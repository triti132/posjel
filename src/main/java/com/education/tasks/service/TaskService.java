package com.education.tasks.service;

import com.education.common.exception.BusinessException;
import com.education.courses.entity.Lesson;
import com.education.courses.service.CourseAccessService;
import com.education.courses.repository.LessonRepository;
import com.education.tasks.dto.TaskAttemptRequest;
import com.education.tasks.dto.TaskAttemptResponse;
import com.education.tasks.dto.TaskCreateRequest;
import com.education.tasks.dto.TaskResponse;
import com.education.tasks.entity.AttemptStatus;
import com.education.tasks.entity.Task;
import com.education.tasks.entity.TaskAttempt;
import com.education.tasks.entity.TaskType;
import com.education.tasks.repository.TaskAttemptRepository;
import com.education.tasks.repository.TaskRepository;
import com.education.users.entity.User;
import com.education.users.repository.UserRepository;
import com.education.users.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskAttemptRepository taskAttemptRepository;
    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;
    private final CourseAccessService courseAccessService;

    @Transactional
    public TaskResponse createTask(Long lessonId, TaskCreateRequest request, CustomUserDetails userDetails) {
        if ("STUDENT".equals(userDetails.getRole())) {
            throw new BusinessException("FORBIDDEN", "Студенты не могут создавать задания");
        }

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new BusinessException("LESSON_NOT_FOUND", "Урок не найден"));

        boolean isAuthor = lesson.getCourse().getAuthor().getId().equals(userDetails.getId());
        boolean isAdmin = "ADMIN".equals(userDetails.getRole());

        if (!isAuthor && !isAdmin) {
            throw new BusinessException("FORBIDDEN", "У вас нет прав на добавление заданий в этот курс");
        }

        Task task = Task.builder()
                .lesson(lesson)
                .taskType(request.taskType())
                .conditionText(request.conditionText())
                .correctAnswer(request.correctAnswer().trim())
                .build();

        return mapToResponse(taskRepository.save(task));
    }

    @Transactional
    public TaskAttemptResponse submitAttempt(Long taskId, TaskAttemptRequest request, CustomUserDetails userDetails) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException("TASK_NOT_FOUND", "Задание не найдено"));

        courseAccessService.checkAccess(task.getLesson().getCourse().getId(), userDetails);
        if (!Boolean.TRUE.equals(task.getLesson().getIsPublished())) {
            throw new BusinessException("ACCESS_DENIED", "Задание недоступно до публикации урока");
        }

        AttemptStatus status = checkAnswer(task, request.studentAnswer());

        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Пользователь не найден"));

        TaskAttempt attempt = TaskAttempt.builder()
                .task(task)
                .user(user)
                .studentAnswer(request.studentAnswer())
                .status(status)
                .build();

        taskAttemptRepository.save(attempt);
        return new TaskAttemptResponse(attempt.getId(), status);
    }

    private AttemptStatus checkAnswer(Task task, String studentAnswer) {
        String normalizedStudent = studentAnswer.trim().toLowerCase().replaceAll("\\s+", " ");
        String normalizedCorrect = task.getCorrectAnswer().trim().toLowerCase().replaceAll("\\s+", " ");

        return switch (task.getTaskType()) {
            case SINGLE_CHOICE_TEST, SHORT_TEXT ->
                    normalizedStudent.equals(normalizedCorrect) ? AttemptStatus.CORRECT : AttemptStatus.INCORRECT;
        };
    }

    private TaskResponse mapToResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getLesson().getId(),
                task.getTaskType(),
                task.getConditionText(),
                task.getCreatedAt()
        );
    }
}
