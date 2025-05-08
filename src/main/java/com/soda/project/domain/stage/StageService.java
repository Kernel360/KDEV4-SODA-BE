package com.soda.project.domain.stage;

import com.soda.global.log.data.annotation.LoggableEntityAction;
import com.soda.project.domain.Project;
import com.soda.project.interfaces.stage.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StageService {
    private final StageProvider stageProvider;
    private final StageValidator stageValidator;

    public Stage validateStage(Long stageId, Project project) {
        return stageValidator.validateStage(stageId, project);
    }

    public Stage getStageOrThrow(Long stageId) {
        return stageProvider.getStageOrThrow(stageId);
    }

    @LoggableEntityAction(action = "CREATE", entityClass = Stage.class)
    @Transactional
    public StageResponse createStage(StageCreateRequest request) {
        Project project = stageProvider.getProjectOrThrow(request.getProjectId());
        stageValidator.validateStageCount(project);
        stageValidator.validateStageName(project, request.getName(), null);
        stageValidator.validateStageOrder(project, request.getPrevStageId(), request.getNextStageId());

        float newOrder = calculateNewOrder(project, request.getPrevStageId(), request.getNextStageId());
        Stage newStage = Stage.builder()
                .project(project)
                .name(request.getName())
                .stageOrder(newOrder)
                .build();

        Stage savedStage = stageProvider.store(newStage);
        log.info("새 단계 생성 완료: stageId={}, projectId={}, name={}", savedStage.getId(), project.getId(),
                request.getName());
        return StageResponse.fromEntity(savedStage);
    }

    public List<StageReadResponse> getStagesByProjectId(Long projectId) {
        Project project = stageProvider.getProjectOrThrow(projectId);
        List<Stage> stages = stageProvider.findByProjectIdAndIsDeletedFalseOrderByStageOrderAsc(projectId);
        log.info("프로젝트 ID {} 의 활성 단계 {}개 조회 성공", projectId, stages.size());

        return stages.stream()
                .map(StageReadResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @LoggableEntityAction(action = "UPDATE", entityClass = Stage.class)
    @Transactional
    public StageResponse updateStage(Long stageId, StageUpdateRequest request) {
        Stage stage = stageProvider.getStageOrThrow(stageId);
        Project project = stage.getProject();
        stageValidator.validateStageName(project, request.getName(), stageId);

        if (!request.getName().equals(stage.getName())) {
            stage.updateName(request.getName());
            log.info("단계 이름 업데이트 완료: stageId={}, oldName={}, newName={}",
                    stageId, stage.getName(), request.getName());
        }

        Stage updatedStage = stageProvider.store(stage);
        return StageResponse.fromEntity(updatedStage);
    }

    @LoggableEntityAction(action = "UPDATE", entityClass = Stage.class)
    @Transactional
    public void moveStage(Long stageId, StageMoveRequest request) {
        Stage stage = stageProvider.getStageOrThrow(stageId);
        Project project = stage.getProject();
        stageValidator.validateStageOrder(project, request.getPrevStageId(), request.getNextStageId());

        float newOrder = calculateNewOrder(project, request.getPrevStageId(), request.getNextStageId());
        log.info("단계 이동 실행: stageId={}, projectId={}, oldOrder={}, newOrder={}",
                stageId, project.getId(), stage.getStageOrder(), newOrder);
        stage.moveStageOrder(newOrder);

        stageProvider.store(stage);
    }

    @LoggableEntityAction(action = "DELETE", entityClass = Stage.class)
    @Transactional
    public void deleteStage(Long stageId) {
        Stage stage = stageProvider.getStageOrThrow(stageId);
        Project project = stage.getProject();
        stage.delete();
        stageProvider.store(stage);
        log.info("단계 삭제 완료: stageId={}, projectId={}", stageId, project.getId());
    }

    private float calculateNewOrder(Project project, Long prevStageId, Long nextStageId) {
        Float prevOrder = null;
        Float nextOrder = null;

        if (prevStageId != null) {
            Stage prevStage = validateStage(prevStageId, project);
            prevOrder = prevStage.getStageOrder();
        }

        if (nextStageId != null) {
            Stage nextStage = validateStage(nextStageId, project);
            nextOrder = nextStage.getStageOrder();
        }

        if (prevOrder == null && nextOrder == null) {
            return StageConstants.INITIAL_ORDER;
        } else if (prevOrder == null) {
            return nextOrder - StageConstants.ORDER_INCREMENT;
        } else if (nextOrder == null) {
            return prevOrder + StageConstants.ORDER_INCREMENT;
        } else {
            return (prevOrder + nextOrder) / 2;
        }
    }
}
