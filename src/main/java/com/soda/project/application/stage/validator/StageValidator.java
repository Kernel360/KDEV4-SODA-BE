package com.soda.project.application.stage.validator;

import com.soda.project.domain.Project;
import com.soda.project.domain.stage.Stage;

public interface StageValidator {
    void validateStageName(Project project, String name, Long stageId);

    float validateAndGetNewOrder(Project project, Long prevStageId, Long nextStageId);

    void validateStageCount(Project project);

    void validateStageProject(Stage stage, Project project);

}