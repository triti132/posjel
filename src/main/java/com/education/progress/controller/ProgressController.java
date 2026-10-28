package com.education.progress.controller;

import com.education.progress.dto.StudentProgressDto;
import com.education.progress.service.ProgressService;
import com.education.users.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;

    @PostMapping("/lessons/{lessonId}/complete")
    public void completeLesson(
            @PathVariable Long lessonId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        progressService.completeLesson(lessonId, userDetails);
    }

    @GetMapping("/teacher/courses/{courseId}/students")
    public List<StudentProgressDto> getCourseStudentsProgress(
            @PathVariable Long courseId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return progressService.getCourseStudentsProgress(courseId, userDetails);
    }
}