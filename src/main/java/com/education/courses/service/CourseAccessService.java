package com.education.courses.service;

import com.education.common.exception.BusinessException;
import com.education.courses.entity.Course;
import com.education.courses.entity.CourseStatus;
import com.education.courses.entity.CourseVisibility;
import com.education.courses.repository.CourseEnrollmentRepository;
import com.education.courses.repository.CourseRepository;
import com.education.users.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CourseAccessService {

    private final CourseRepository courseRepository;
    private final CourseEnrollmentRepository enrollmentRepository;

    public void checkAccess(Long courseId, CustomUserDetails userDetails) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException("COURSE_NOT_FOUND", "Курс не найден"));

        if (!hasAccess(course, userDetails)) {
            throw new BusinessException("ACCESS_DENIED", "Доступ к курсу запрещён");
        }
    }

    public boolean hasAccess(Course course, CustomUserDetails userDetails) {
        // ИСПРАВЛЕНО: сравниваем строки через .equals()
        if ("ADMIN".equals(userDetails.getRole())) {
            return true;
        }

        if (course.getAuthor().getId().equals(userDetails.getId())) {
            return true;
        }

        if (course.getStatus() != CourseStatus.PUBLISHED) {
            return false;
        }

        if (course.getVisibility() == CourseVisibility.PUBLIC) {
            return true;
        }

        return enrollmentRepository.existsByCourseIdAndUserId(course.getId(), userDetails.getId());
    }

    public void checkEditAccess(Long courseId, CustomUserDetails userDetails) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException("COURSE_NOT_FOUND", "Курс не найден"));

        // ИСПРАВЛЕНО: сравниваем строки через .equals()
        if (!course.getAuthor().getId().equals(userDetails.getId()) && !"ADMIN".equals(userDetails.getRole())) {
            throw new BusinessException("ACCESS_DENIED", "Только автор курса или администратор может редактировать");
        }
    }
}
