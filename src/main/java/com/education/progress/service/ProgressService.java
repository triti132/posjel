package com.education.progress.service;

import com.education.common.exception.BusinessException;
import com.education.courses.entity.Course;
import com.education.courses.entity.Lesson;
import com.education.courses.repository.CourseRepository;
import com.education.courses.repository.LessonRepository;
import com.education.courses.service.CourseAccessService;
import com.education.progress.dto.StudentProgressDto;
import com.education.progress.entity.LessonProgress;
import com.education.progress.repository.LessonProgressRepository;
import com.education.tasks.repository.TaskAttemptRepository;
import com.education.tasks.repository.TaskRepository;
import com.education.users.entity.User;
import com.education.users.repository.UserRepository;
import com.education.users.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProgressService {

    private final LessonProgressRepository lessonProgressRepository;
    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;
    private final TaskRepository taskRepository;
    private final TaskAttemptRepository taskAttemptRepository;
    private final UserRepository userRepository;
    private final CourseAccessService courseAccessService;

    @Transactional
    public void completeLesson(Long lessonId, CustomUserDetails userDetails) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new BusinessException("LESSON_NOT_FOUND", "Урок не найден"));

        courseAccessService.checkAccess(lesson.getCourse().getId(), userDetails);
        if (!Boolean.TRUE.equals(lesson.getIsPublished())) {
            throw new BusinessException("ACCESS_DENIED", "Нельзя завершить неопубликованный урок");
        }

        // ТЗ: Нельзя завершить урок "задним числом" без попытки, если в нем есть задания
        long taskCount = taskRepository.countByLessonId(lessonId);
        if (taskCount > 0) {
            boolean hasAttempt = taskAttemptRepository.existsByTask_Lesson_IdAndUser_Id(lessonId, userDetails.getId());
            if (!hasAttempt) {
                throw new BusinessException("FORBIDDEN", "Невозможно завершить урок: необходимо выполнить хотя бы одно задание");
            }
        }

        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Пользователь не найден"));

        LessonProgress progress = lessonProgressRepository.findByLessonIdAndUserId(lessonId, userDetails.getId())
                .orElse(LessonProgress.builder()
                        .lesson(lessonRepository.getReferenceById(lessonId))
                        .user(user)
                        .isCompleted(false)
                        .build());

        if (!progress.getIsCompleted()) {
            progress.setIsCompleted(true);
            progress.setCompletedAt(LocalDateTime.now());
            lessonProgressRepository.save(progress);
        }
    }

    @Transactional(readOnly = true)
    public List<StudentProgressDto> getCourseStudentsProgress(Long courseId, CustomUserDetails userDetails) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException("COURSE_NOT_FOUND", "Курс не найден"));

        boolean isAuthor = course.getAuthor().getId().equals(userDetails.getId());
        boolean isAdmin = "ADMIN".equals(userDetails.getRole());

        if (!isAuthor && !isAdmin) {
            throw new BusinessException("FORBIDDEN", "У вас нет прав на просмотр прогресса этого курса");
        }

        long totalPublishedLessons = lessonRepository.countByCourseIdAndIsPublishedTrue(courseId);
        List<LessonProgress> allProgress = lessonProgressRepository.findByLesson_Course_Id(courseId);

        Map<Long, List<LessonProgress>> userProgressMap = allProgress.stream()
                .collect(Collectors.groupingBy(lp -> lp.getUser().getId()));

        return userProgressMap.entrySet().stream().map(entry -> {
            List<LessonProgress> userLessons = entry.getValue();
            User user = userLessons.get(0).getUser();
            long completedCount = userLessons.stream().filter(LessonProgress::getIsCompleted).count();

            double percentage = totalPublishedLessons > 0
                    ? (completedCount * 100.0) / totalPublishedLessons
                    : 0.0;

            return new StudentProgressDto(
                    user.getId(),
                    user.getFullName(),
                    user.getEmail(),
                    completedCount,
                    totalPublishedLessons,
                    Math.round(percentage * 100.0) / 100.0
            );
        }).toList();
    }
}
