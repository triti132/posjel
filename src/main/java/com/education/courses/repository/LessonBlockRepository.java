package com.education.courses.repository;

import com.education.courses.entity.LessonBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonBlockRepository extends JpaRepository<LessonBlock, Long> {

    List<LessonBlock> findByLessonIdOrderByOrderIndexAsc(Long lessonId);
}
