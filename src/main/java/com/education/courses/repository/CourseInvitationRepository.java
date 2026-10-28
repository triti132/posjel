package com.education.courses.repository;

import com.education.courses.entity.CourseInvitation;
import com.education.courses.entity.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseInvitationRepository extends JpaRepository<CourseInvitation, Long> {

    Optional<CourseInvitation> findByToken(String token);

    boolean existsByCourseIdAndEmailAndStatus(Long courseId, String email, InvitationStatus status);

    // ДОБАВЛЕНО: этот метод искал компилятор
    Optional<CourseInvitation> findByCourseIdAndEmailAndStatus(Long courseId, String email, InvitationStatus status);

    List<CourseInvitation> findByCourseIdAndStatus(Long courseId, InvitationStatus status);

    List<CourseInvitation> findByCourseId(Long courseId);
}