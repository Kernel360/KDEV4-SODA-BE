package com.soda.project;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StageRepository extends JpaRepository<Stage, Long> {
    List<Stage> findByProjectIdAndIsDeletedFalseOrderByStageOrderAsc(Long projectId);

    int countByProjectAndIsDeletedFalse(Project project);

    Optional<Stage> findByIdAndIsDeletedFalse(Long stageId);

    boolean existsByIdAndIsDeletedFalse(Long stageId);
}
