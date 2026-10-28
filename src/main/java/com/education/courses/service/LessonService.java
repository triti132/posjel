package com.education.courses.service;

import com.education.common.exception.BusinessException;
import com.education.courses.dto.LessonBlockResponse;
import com.education.courses.dto.LessonCreateRequest;
import com.education.courses.dto.LessonResponse;
import com.education.courses.dto.LessonUpdateRequest;
import com.education.courses.entity.Course;
import com.education.courses.entity.Lesson;
import com.education.users.entity.Role; // <-- ДОБАВЛЕН ИМПОРТ
import com.education.courses.repository.CourseRepository;
import com.education.courses.repository.LessonRepository;
import com.education.users.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// ... (импорты остаются теми же)

@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;
    private final CourseAccessService courseAccessService;

    @Transactional
    public LessonResponse createLesson(Long courseId, LessonCreateRequest request, CustomUserDetails userDetails) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException("COURSE_NOT_FOUND", "Курс не найден"));

        checkCourseOwnership(course, userDetails);

        Lesson lesson = Lesson.builder()
                .course(course)
                .title(request.getTitle())
                .description(request.getDescription())
                .orderIndex(request.getOrderIndex())
                .isPublished(false)
                .build();

        return mapToResponse(lessonRepository.save(lesson));
    }

    @Transactional(readOnly = true)
    public List<LessonResponse> getLessonsByCourse(Long courseId, CustomUserDetails userDetails) {
        courseAccessService.checkAccess(courseId, userDetails);

        boolean canViewDraftLessons = isCourseAuthorOrAdmin(courseRepository.getReferenceById(courseId), userDetails);
        return lessonRepository.findByCourseIdOrderByOrderIndexAsc(courseId)
                .stream()
                .filter(lesson -> canViewDraftLessons || Boolean.TRUE.equals(lesson.getIsPublished()))
                .map(this::mapToResponse)
                .toList();
    }

    // ИСПРАВЛЕНО: добавлен параметр userDetails
    @Transactional(readOnly = true)
    public LessonResponse getLesson(Long lessonId, CustomUserDetails userDetails) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new BusinessException("LESSON_NOT_FOUND", "Урок не найден"));

        courseAccessService.checkAccess(lesson.getCourse().getId(), userDetails);

        if (!Boolean.TRUE.equals(lesson.getIsPublished()) && !isCourseAuthorOrAdmin(lesson.getCourse(), userDetails)) {
            throw new BusinessException("ACCESS_DENIED", "Доступ к уроку запрещён");
        }

        return mapToResponse(lesson);
    }

    @Transactional
    public LessonResponse updateLesson(Long lessonId, LessonUpdateRequest request, CustomUserDetails userDetails) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new BusinessException("LESSON_NOT_FOUND", "Урок не найден"));

        checkCourseOwnership(lesson.getCourse(), userDetails);

        lesson.setTitle(request.getTitle());
        lesson.setDescription(request.getDescription());
        lesson.setOrderIndex(request.getOrderIndex());
        if (request.getIsPublished() != null) {
            lesson.setIsPublished(request.getIsPublished());
        }

        return mapToResponse(lessonRepository.save(lesson));
    }

    @Transactional
    public void deleteLesson(Long lessonId, CustomUserDetails userDetails) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new BusinessException("LESSON_NOT_FOUND", "Урок не найден"));

        checkCourseOwnership(lesson.getCourse(), userDetails);

        lessonRepository.delete(lesson);
    }

    private void checkCourseOwnership(Course course, CustomUserDetails userDetails) {
        if (!isCourseAuthorOrAdmin(course, userDetails)) {
            throw new BusinessException("FORBIDDEN", "У вас нет прав на изменение этого курса");
        }
    }

    private boolean isCourseAuthorOrAdmin(Course course, CustomUserDetails userDetails) {
        return course.getAuthor().getId().equals(userDetails.getId())
                || Role.ADMIN.name().equals(userDetails.getRole());
    }

    private LessonResponse mapToResponse(Lesson lesson) {
        List<LessonBlockResponse> blockResponses = lesson.getBlocks() != null
                ? lesson.getBlocks().stream()
                .map(block -> LessonBlockResponse.builder()
                        .id(block.getId())
                        .lessonId(block.getLesson().getId())
                        .blockType(block.getBlockType())
                        .content(block.getContent())
                        .orderIndex(block.getOrderIndex())
                        .createdAt(block.getCreatedAt())
                        .updatedAt(block.getUpdatedAt())
                        .build())
                .toList()
                : List.of();

        return LessonResponse.builder()
                .id(lesson.getId())
                .courseId(lesson.getCourse().getId())
                .title(lesson.getTitle())
                .description(lesson.getDescription())
                .orderIndex(lesson.getOrderIndex())
                .isPublished(lesson.getIsPublished())
                .blocks(blockResponses)
                .createdAt(lesson.getCreatedAt())
                .updatedAt(lesson.getUpdatedAt())
                .build();
    }
}
