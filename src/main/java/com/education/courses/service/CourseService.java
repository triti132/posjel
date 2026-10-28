package com.education.courses.service;

import com.education.common.exception.BusinessException;
import com.education.courses.dto.*;
import com.education.courses.entity.Course;
import com.education.courses.entity.CourseStatus;
import com.education.courses.entity.CourseVisibility;
import com.education.courses.repository.CourseRepository;
import com.education.users.entity.Role;
import com.education.users.entity.User;
import com.education.users.repository.UserRepository;
import com.education.users.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final CourseAccessService courseAccessService;

    public List<CourseResponse> getPublicCatalog(CustomUserDetails userDetails) {
        List<Course> courses;

        if (userDetails == null) {
            // ИСПРАВЛЕНО: правильное имя метода
            courses = courseRepository.findByStatusAndVisibility(CourseStatus.PUBLISHED, CourseVisibility.PUBLIC);
        } else {
            courses = courseRepository.findAccessibleCourses(userDetails.getId());
        }

        return courses.stream()
                .map(this::mapToResponse)
                .toList();
    }

    public CourseResponse getCourse(Long id, CustomUserDetails userDetails) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new BusinessException("COURSE_NOT_FOUND", "Курс не найден"));

        if (userDetails != null) {
            courseAccessService.checkAccess(id, userDetails);
        } else if (course.getVisibility() != CourseVisibility.PUBLIC || course.getStatus() != CourseStatus.PUBLISHED) {
            throw new BusinessException("ACCESS_DENIED", "Доступ к курсу запрещён");
        }

        return mapToResponse(course);
    }

    @Transactional
    public CourseResponse createCourse(CourseCreateRequest request, Long authorId) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Пользователь не найден"));

        if (author.getRole() == Role.STUDENT) {
            throw new BusinessException("FORBIDDEN", "Только преподаватели и администраторы могут создавать курсы");
        }

        Course course = Course.builder()
                .title(request.title())
                .description(request.description())
                .coverUrl(request.coverUrl())
                .discipline(request.discipline())
                .status(CourseStatus.DRAFT)
                .visibility(request.visibility() != null ? request.visibility() : CourseVisibility.PUBLIC)
                .author(author)
                .build();

        course = courseRepository.save(course);
        return mapToResponse(course);
    }

    @Transactional
    public CourseResponse updateCourse(Long id, CourseUpdateRequest request, CustomUserDetails userDetails) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new BusinessException("COURSE_NOT_FOUND", "Курс не найден"));

        courseAccessService.checkEditAccess(id, userDetails);

        if (request.title() != null) course.setTitle(request.title());
        if (request.description() != null) course.setDescription(request.description());
        if (request.coverUrl() != null) course.setCoverUrl(request.coverUrl());
        if (request.discipline() != null) course.setDiscipline(request.discipline());
        if (request.visibility() != null) course.setVisibility(request.visibility());

        course = courseRepository.save(course);
        return mapToResponse(course);
    }

    @Transactional
    public CourseResponse publishCourse(Long id, CustomUserDetails userDetails) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new BusinessException("COURSE_NOT_FOUND", "Курс не найден"));

        courseAccessService.checkEditAccess(id, userDetails);

        course.setStatus(CourseStatus.PUBLISHED);
        course = courseRepository.save(course);
        return mapToResponse(course);
    }

    @Transactional
    public CourseResponse copyCourse(Long id, CustomUserDetails userDetails) {
        Course original = courseRepository.findById(id)
                .orElseThrow(() -> new BusinessException("COURSE_NOT_FOUND", "Курс не найден"));

        if (!courseAccessService.hasAccess(original, userDetails)) {
            throw new BusinessException("ACCESS_DENIED", "Доступ к курсу запрещён");
        }

        User newAuthor = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Пользователь не найден"));

        Course copy = Course.builder()
                .title(original.getTitle() + " (Копия)")
                .description(original.getDescription())
                .coverUrl(original.getCoverUrl())
                .discipline(original.getDiscipline())
                .status(CourseStatus.DRAFT)
                .visibility(original.getVisibility())
                .author(newAuthor)
                .build();

        copy = courseRepository.save(copy);
        return mapToResponse(copy);
    }

    private CourseResponse mapToResponse(Course course) {
        return new CourseResponse(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getCoverUrl(),
                course.getDiscipline(),
                course.getStatus(),
                course.getVisibility(),
                course.getAuthor().getId(),        // ИСПРАВЛЕНО: было getFullName()
                course.getAuthor().getFullName(),  // ИСПРАВЛЕНО: было getCreatedAt()
                course.getCreatedAt(),
                course.getUpdatedAt()              // ИСПРАВЛЕНО: добавлено
        );
    }
}
