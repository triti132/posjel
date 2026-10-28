package com.education.courses.controller;

import com.education.courses.dto.*;
import com.education.courses.service.LessonBlockService;
import com.education.courses.service.LessonService;
import com.education.users.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class LessonController {

    private final LessonService lessonService;
    private final LessonBlockService lessonBlockService;

    @PostMapping("/courses/{courseId}/lessons")
    public ResponseEntity<LessonResponse> createLesson(
            @PathVariable Long courseId,
            @Valid @RequestBody LessonCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(lessonService.createLesson(courseId, request, userDetails));
    }

    @GetMapping("/courses/{courseId}/lessons")
    public ResponseEntity<List<LessonResponse>> getLessonsByCourse(
            @PathVariable Long courseId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(lessonService.getLessonsByCourse(courseId, userDetails));
    }

    // ИСПРАВЛЕНО: добавлен параметр userDetails
    @GetMapping("/lessons/{lessonId}")
    public ResponseEntity<LessonResponse> getLesson(
            @PathVariable Long lessonId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(lessonService.getLesson(lessonId, userDetails));
    }

    @PutMapping("/lessons/{lessonId}")
    public ResponseEntity<LessonResponse> updateLesson(
            @PathVariable Long lessonId,
            @Valid @RequestBody LessonUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(lessonService.updateLesson(lessonId, request, userDetails));
    }

    @DeleteMapping("/lessons/{lessonId}")
    public ResponseEntity<Void> deleteLesson(
            @PathVariable Long lessonId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        lessonService.deleteLesson(lessonId, userDetails);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/lessons/{lessonId}/blocks")
    public ResponseEntity<LessonBlockResponse> addBlock(
            @PathVariable Long lessonId,
            @Valid @RequestBody LessonBlockCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(lessonBlockService.addBlock(lessonId, request, userDetails));
    }

    @PutMapping("/lessons/blocks/{blockId}")
    public ResponseEntity<LessonBlockResponse> updateBlock(
            @PathVariable Long blockId,
            @Valid @RequestBody LessonBlockUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(lessonBlockService.updateBlock(blockId, request, userDetails));
    }

    @DeleteMapping("/lessons/blocks/{blockId}")
    public ResponseEntity<Void> deleteBlock(
            @PathVariable Long blockId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        lessonBlockService.deleteBlock(blockId, userDetails);
        return ResponseEntity.noContent().build();
    }
}
