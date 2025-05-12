package com.soda.project.application.stage;

import com.soda.global.log.data.annotation.LoggableEntityAction;
import com.soda.global.response.GeneralException;
import com.soda.project.application.stage.validator.StageValidator;
import com.soda.project.domain.Project;
import com.soda.project.domain.ProjectService;
import com.soda.project.domain.stage.Stage;
import com.soda.project.domain.stage.StageErrorCode;
import com.soda.project.domain.stage.StageService;
import com.soda.project.interfaces.stage.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StageFacade {
    private final StageService stageService;
    private final ProjectService projectService;
    private final StageValidator stageValidator;

    @Transactional
    @LoggableEntityAction(action = "CREATE", entityClass = Stage.class)
    public StageResponse addStage(StageCreateRequest request) {
        Project project = projectService.getValidProject(request.getProjectId());

        stageValidator.validateStageCount(project);
        stageValidator.validateStageName(project, request.getName(), null);
        float newOrder = stageValidator.validateAndGetNewOrder(project, request.getPrevStageId(), request.getNextStageId());

        return stageService.createStage(project, request.getName(), newOrder);
    }

    public List<StageReadResponse> getStages(Long projectId) {
        return stageService.getStagesByProjectId(projectId);
    }

    @Transactional
    @LoggableEntityAction(action = "UPDATE", entityClass = Stage.class)
    public void moveStage(Long stageId, StageMoveRequest request) {
        if (stageId == null) {
            throw new GeneralException(StageErrorCode.STAGE_ID_REQUIRED);
        }
        if (Objects.equals(stageId, request.getPrevStageId()) || Objects.equals(stageId, request.getNextStageId())) {
            throw new GeneralException(StageErrorCode.CANNOT_MOVE_STAGE_RELATIVE_TO_ITSELF);
        }

        Stage stageToMove = stageService.getStageOrThrow(stageId);
        Project project = stageToMove.getProject();

        float newOrder = stageValidator.validateAndGetNewOrder(project, request.getPrevStageId(), request.getNextStageId());

        stageService.moveStage(stageToMove, newOrder);
    }

    @Transactional
    @LoggableEntityAction(action = "DELETE", entityClass = Stage.class)
    public void deleteStage(Long stageId) {
        if (stageId == null) {
            throw new GeneralException(StageErrorCode.STAGE_ID_REQUIRED);
        }
        stageService.deleteStage(stageId);
    }

    @Transactional
    @LoggableEntityAction(action = "UPDATE", entityClass = Stage.class)
    public StageResponse updateStage(Long stageId, StageUpdateRequest request) {
        if (stageId == null) {
            throw new GeneralException(StageErrorCode.STAGE_ID_REQUIRED);
        }

        Stage stageToUpdate = stageService.getStageOrThrow(stageId);
        Project project = stageToUpdate.getProject();

        stageValidator.validateStageName(project, request.getName(), stageId);

        return stageService.updateStage(stageToUpdate, request.getName());
    }
}