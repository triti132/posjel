package com.education.tasks.repository;

import com.education.tasks.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {

    long countByLessonId(Long lessonId);
}
