package com.education.courses.service;

import com.education.common.exception.BusinessException;
import com.education.courses.dto.LessonBlockCreateRequest;
import com.education.courses.dto.LessonBlockResponse;
import com.education.courses.dto.LessonBlockUpdateRequest;
import com.education.courses.entity.Course;
import com.education.courses.entity.Lesson;
import com.education.courses.entity.LessonBlock;
import com.education.courses.repository.LessonBlockRepository;
import com.education.courses.repository.LessonRepository;
import com.education.users.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LessonBlockService {

    private final LessonBlockRepository lessonBlockRepository;
    private final LessonRepository lessonRepository;

    @Transactional
    public LessonBlockResponse addBlock(Long lessonId, LessonBlockCreateRequest request, CustomUserDetails userDetails) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new BusinessException("LESSON_NOT_FOUND", "Урок не найден"));

        checkCourseOwnership(lesson.getCourse(), userDetails);

        LessonBlock block = LessonBlock.builder()
                .lesson(lesson)
                .blockType(request.getBlockType())
                .content(request.getContent())
                .orderIndex(request.getOrderIndex())
                .build();

        return mapToResponse(lessonBlockRepository.save(block));
    }

    @Transactional
    public LessonBlockResponse updateBlock(Long blockId, LessonBlockUpdateRequest request, CustomUserDetails userDetails) {
        LessonBlock block = lessonBlockRepository.findById(blockId)
                .orElseThrow(() -> new BusinessException("BLOCK_NOT_FOUND", "Блок не найден"));

        checkCourseOwnership(block.getLesson().getCourse(), userDetails);

        block.setBlockType(request.getBlockType());
        block.setContent(request.getContent());
        block.setOrderIndex(request.getOrderIndex());

        return mapToResponse(lessonBlockRepository.save(block));
    }

    @Transactional
    public void deleteBlock(Long blockId, CustomUserDetails userDetails) {
        LessonBlock block = lessonBlockRepository.findById(blockId)
                .orElseThrow(() -> new BusinessException("BLOCK_NOT_FOUND", "Блок не найден"));

        checkCourseOwnership(block.getLesson().getCourse(), userDetails);

        lessonBlockRepository.delete(block);
    }

    private void checkCourseOwnership(Course course, CustomUserDetails userDetails) {
        boolean isAuthor = course.getAuthor().getId().equals(userDetails.getId());
        boolean isAdmin = "ADMIN".equals(userDetails.getRole());

        if (!isAuthor && !isAdmin) {
            throw new BusinessException("FORBIDDEN", "У вас нет прав на изменение этого курса");
        }
    }

    private LessonBlockResponse mapToResponse(LessonBlock block) {
        return LessonBlockResponse.builder()
                .id(block.getId())
                .lessonId(block.getLesson().getId())
                .blockType(block.getBlockType())
                .content(block.getContent())
                .orderIndex(block.getOrderIndex())
                .createdAt(block.getCreatedAt())
                .updatedAt(block.getUpdatedAt())
                .build();
    }
}