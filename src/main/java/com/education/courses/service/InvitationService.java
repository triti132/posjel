package com.education.courses.service;

import com.education.common.exception.BusinessException;
import com.education.courses.dto.InvitationRequest;
import com.education.courses.dto.InvitationResponse;
import com.education.courses.entity.Course;
import com.education.courses.entity.CourseEnrollment;
import com.education.courses.entity.CourseInvitation;
import com.education.courses.entity.InvitationStatus;
import com.education.courses.repository.CourseEnrollmentRepository;
import com.education.courses.repository.CourseInvitationRepository;
import com.education.courses.repository.CourseRepository;
import com.education.users.entity.User;
import com.education.users.repository.UserRepository;
import com.education.users.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvitationService {

    private final CourseRepository courseRepository;
    private final CourseInvitationRepository invitationRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final CourseAccessService courseAccessService; // ТЕПЕРЬ ИСПОЛЬЗУЕТСЯ

    @Transactional
    public InvitationResponse createInvitation(Long courseId, InvitationRequest request, CustomUserDetails userDetails) {
        // ИСПРАВЛЕНО: используем сервис вместо ручной проверки (убирает warning)
        courseAccessService.checkEditAccess(courseId, userDetails);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException("COURSE_NOT_FOUND", "Курс не найден"));

        if (course.getVisibility() != com.education.courses.entity.CourseVisibility.PRIVATE) {
            throw new BusinessException("COURSE_NOT_PRIVATE", "Приглашения можно отправлять только в приватные курсы");
        }

        String normalizedEmail = request.email().toLowerCase().trim();

        userRepository.findByEmailIgnoreCase(normalizedEmail).ifPresent(user -> {
            if (enrollmentRepository.existsByCourseIdAndUserId(courseId, user.getId())) {
                throw new BusinessException("ALREADY_ENROLLED", "Пользователь уже записан на курс");
            }
        });

        if (invitationRepository.existsByCourseIdAndEmailAndStatus(courseId, normalizedEmail, InvitationStatus.PENDING)) {
            throw new BusinessException("INVITATION_EXISTS", "Приглашение уже отправлено");
        }

        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(7);

        CourseInvitation invitation = CourseInvitation.builder()
                .course(course)
                .email(normalizedEmail)
                .invitedBy(User.builder().id(userDetails.getId()).build())
                .token(token)
                .status(InvitationStatus.PENDING)
                .expiresAt(expiresAt)
                .createdAt(LocalDateTime.now())
                .build();

        invitation = invitationRepository.save(invitation);

        return new InvitationResponse(
                invitation.getId(),
                invitation.getEmail(),
                invitation.getStatus(),
                invitation.getExpiresAt(),
                invitation.getCreatedAt()
        );
    }

    @Transactional
    public void acceptInvitation(String token, CustomUserDetails userDetails) {
        CourseInvitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new BusinessException("INVITATION_NOT_FOUND", "Приглашение не найдено"));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new BusinessException("INVITATION_INVALID", "Приглашение недействительно");
        }

        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            invitation.setStatus(InvitationStatus.EXPIRED);
            invitationRepository.save(invitation);
            throw new BusinessException("INVITATION_EXPIRED", "Срок действия приглашения истёк");
        }

        if (!invitation.getEmail().equalsIgnoreCase(userDetails.getEmail())) {
            throw new BusinessException("EMAIL_MISMATCH", "Приглашение предназначено для другого email");
        }

        if (!enrollmentRepository.existsByCourseIdAndUserId(invitation.getCourse().getId(), userDetails.getId())) {
            CourseEnrollment enrollment = CourseEnrollment.builder()
                    .course(invitation.getCourse())
                    .user(User.builder().id(userDetails.getId()).build())
                    .enrolledAt(LocalDateTime.now())
                    .build();
            enrollmentRepository.save(enrollment);
        }

        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitation.setAcceptedAt(LocalDateTime.now());
        invitationRepository.save(invitation);
    }

    public List<InvitationResponse> getCourseInvitations(Long courseId, CustomUserDetails userDetails) {
        // ИСПРАВЛЕНО: используем сервис вместо ручной проверки (убирает warning)
        courseAccessService.checkEditAccess(courseId, userDetails);

        return invitationRepository.findByCourseId(courseId).stream()
                .map(inv -> new InvitationResponse(
                        inv.getId(),
                        inv.getEmail(),
                        inv.getStatus(),
                        inv.getExpiresAt(),
                        inv.getCreatedAt()
                ))
                .toList();
    }
}
