package com.education.courses.controller;

import com.education.courses.dto.*;
import com.education.courses.service.CourseService;
import com.education.courses.service.InvitationService;
import com.education.users.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final InvitationService invitationService;

    @GetMapping
    public ResponseEntity<List<CourseResponse>> getCatalog(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(courseService.getPublicCatalog(userDetails));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseResponse> getCourse(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(courseService.getCourse(id, userDetails));
    }

    @PostMapping
    public ResponseEntity<CourseResponse> createCourse(
            @Valid @RequestBody CourseCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return new ResponseEntity<>(courseService.createCourse(request, userDetails.getId()), HttpStatus.CREATED);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CourseResponse> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody CourseUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(courseService.updateCourse(id, request, userDetails));
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<CourseResponse> publishCourse(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(courseService.publishCourse(id, userDetails));
    }

    @PostMapping("/{id}/copy")
    public ResponseEntity<CourseResponse> copyCourse(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return new ResponseEntity<>(courseService.copyCourse(id, userDetails), HttpStatus.CREATED);
    }

    @PostMapping("/{id}/invitations")
    public ResponseEntity<InvitationResponse> createInvitation(
            @PathVariable Long id,
            @Valid @RequestBody InvitationRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return new ResponseEntity<>(invitationService.createInvitation(id, request, userDetails), HttpStatus.CREATED);
    }

    @GetMapping("/{id}/invitations")
    public ResponseEntity<List<InvitationResponse>> getInvitations(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(invitationService.getCourseInvitations(id, userDetails));
    }
}
