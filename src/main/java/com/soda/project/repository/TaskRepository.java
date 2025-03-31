package com.soda.project.repository;

import com.soda.project.entity.Stage;
import com.soda.project.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    Optional<Task> findById(Long taskId);

    List<Task> findByStageIdAndIsDeletedFalseOrderByTaskOrderAsc(Long stageId);

    int countByStageAndIsDeletedFalse(Stage stage);

    Optional<Task> findByIdAndIsDeletedFalse(Long id);

    boolean existsByIdAndIsDeletedFalse(Long id);

    boolean existsByStageAndIsDeletedFalse(Stage stage);
}
