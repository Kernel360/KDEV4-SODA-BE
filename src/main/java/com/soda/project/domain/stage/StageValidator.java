package com.soda.project.domain.stage;

import com.soda.project.domain.Project;

public interface StageValidator {
    void validateStageName(Project project, String name, Long stageId);

    void validateStageOrder(Project project, Long prevStageId, Long nextStageId);

    void validateStageCount(Project project);

    void validateStageProject(Stage stage, Project project);

    Stage validateStage(Long stageId, Project project);
}