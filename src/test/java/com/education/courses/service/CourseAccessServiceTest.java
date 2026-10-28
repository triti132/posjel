package com.education.courses.service;

import com.education.courses.entity.Course;
import com.education.courses.entity.CourseStatus;
import com.education.courses.entity.CourseVisibility;
import com.education.courses.repository.CourseEnrollmentRepository;
import com.education.courses.repository.CourseRepository;
import com.education.users.entity.Role;
import com.education.users.entity.User;
import com.education.users.security.CustomUserDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CourseAccessServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private CourseEnrollmentRepository enrollmentRepository;

    @InjectMocks
    private CourseAccessService courseAccessService;

    @Test
    void enrolledStudentCannotAccessDraftCourse() {
        User author = User.builder().id(1L).role(Role.TEACHER).build();
        User student = User.builder().id(2L).role(Role.STUDENT).build();
        Course course = Course.builder()
                .id(10L)
                .author(author)
                .status(CourseStatus.DRAFT)
                .visibility(CourseVisibility.PRIVATE)
                .build();

        assertThat(courseAccessService.hasAccess(course, new CustomUserDetails(student))).isFalse();

        verify(enrollmentRepository, never()).existsByCourseIdAndUserId(10L, 2L);
    }

    @Test
    void publishedPublicCourseIsAccessibleToStudent() {
        User author = User.builder().id(1L).role(Role.TEACHER).build();
        User student = User.builder().id(2L).role(Role.STUDENT).build();
        Course course = Course.builder()
                .id(10L)
                .author(author)
                .status(CourseStatus.PUBLISHED)
                .visibility(CourseVisibility.PUBLIC)
                .build();

        assertThat(courseAccessService.hasAccess(course, new CustomUserDetails(student))).isTrue();

        verify(enrollmentRepository, never()).existsByCourseIdAndUserId(10L, 2L);
    }
}
