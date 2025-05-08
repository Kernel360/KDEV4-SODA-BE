package com.soda.project.application.stage;

import com.soda.global.log.data.annotation.LoggableEntityAction;
import com.soda.project.domain.stage.Stage;
import com.soda.project.domain.stage.StageService;
import com.soda.project.interfaces.stage.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StageFacade {
    private final StageService stageService;

    @LoggableEntityAction(action = "CREATE", entityClass = Stage.class)
    @Transactional
    public StageResponse addStage(StageCreateRequest request) {
        return stageService.createStage(request);
    }

    public List<StageReadResponse> getStages(Long projectId) {
        return stageService.getStagesByProjectId(projectId);
    }

    @LoggableEntityAction(action = "UPDATE", entityClass = Stage.class)
    @Transactional
    public void moveStage(Long stageId, StageMoveRequest request) {
        stageService.moveStage(stageId, request);
    }

    @LoggableEntityAction(action = "DELETE", entityClass = Stage.class)
    @Transactional
    public void deleteStage(Long stageId) {
        stageService.deleteStage(stageId);
    }

    @LoggableEntityAction(action = "UPDATE", entityClass = Stage.class)
    @Transactional
    public StageResponse updateStage(Long stageId, StageUpdateRequest request) {
        return stageService.updateStage(stageId, request);
    }
}