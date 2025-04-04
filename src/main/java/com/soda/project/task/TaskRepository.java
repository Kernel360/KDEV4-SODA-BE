package com.soda.project.task;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    Optional<Task> findById(Long taskId);

    List<Task> findByStageIdAndIsDeletedFalseOrderByTaskOrderAsc(Long stageId);

    Optional<Task> findByIdAndIsDeletedFalse(Long id);
}
