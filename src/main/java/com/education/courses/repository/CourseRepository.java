package com.education.courses.repository;

import com.education.courses.entity.Course;
import com.education.courses.entity.CourseStatus;
import com.education.courses.entity.CourseVisibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    @Query("SELECT c FROM Course c JOIN FETCH c.author WHERE c.status = :status AND c.visibility = :visibility")
    List<Course> findByStatusAndVisibility(@Param("status") CourseStatus status,
                                           @Param("visibility") CourseVisibility visibility);

    @Query("SELECT c FROM Course c JOIN FETCH c.author WHERE c.author.id = :authorId")
    List<Course> findByAuthorId(@Param("authorId") Long authorId);

    @Query("SELECT c FROM Course c JOIN FETCH c.author WHERE " +
            "(c.visibility = 'PUBLIC' AND c.status = 'PUBLISHED') OR " +
            "(c.author.id = :userId) OR " +
            "(c.status = 'PUBLISHED' AND c.id IN " +
            "(SELECT e.course.id FROM CourseEnrollment e WHERE e.user.id = :userId))")
    List<Course> findAccessibleCourses(@Param("userId") Long userId);
}
