package com.education.tasks.controller;

import com.education.tasks.dto.TaskAttemptRequest;
import com.education.tasks.dto.TaskAttemptResponse;
import com.education.tasks.dto.TaskCreateRequest;
import com.education.tasks.dto.TaskResponse;
import com.education.tasks.service.TaskService;
import com.education.users.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping("/lessons/{lessonId}/tasks")
    public TaskResponse createTask(
            @PathVariable Long lessonId,
            @Valid @RequestBody TaskCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return taskService.createTask(lessonId, request, userDetails);
    }

    @PostMapping("/tasks/{taskId}/attempts")
    public TaskAttemptResponse submitAttempt(
            @PathVariable Long taskId,
            @Valid @RequestBody TaskAttemptRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return taskService.submitAttempt(taskId, request, userDetails);
    }
}