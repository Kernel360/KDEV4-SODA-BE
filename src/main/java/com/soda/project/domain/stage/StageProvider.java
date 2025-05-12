package com.soda.project.domain.stage;

import com.soda.project.domain.Project;

import java.util.List;
import java.util.Optional;

public interface StageProvider {
    Stage store(Stage stage);

    List<Stage> storeAll(List<Stage> stages);

    Optional<Stage> findById(Long id);

    List<Stage> findByProjectIdAndIsDeletedFalseOrderByStageOrderAsc(Long projectId);

    boolean existsByProjectAndNameAndIsDeletedFalseAndIdNot(Project project, String name, Long stageId);

    Project getProjectOrThrow(Long projectId);

    Stage getStageOrThrow(Long stageId);

    int countByProjectAndIsDeletedFalse(Project project);
}