package com.education.tasks.repository;

import com.education.tasks.entity.TaskAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskAttemptRepository extends JpaRepository<TaskAttempt, Long> {

    boolean existsByTask_Lesson_IdAndUser_Id(Long lessonId, Long userId);
    long countByUser_IdAndTask_Lesson_Course_Id(Long userId, Long courseId);
}
