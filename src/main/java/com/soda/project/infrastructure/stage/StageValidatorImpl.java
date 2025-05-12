package com.soda.project.infrastructure.stage;

import com.soda.global.response.GeneralException;
import com.soda.project.application.stage.validator.StageValidator;
import com.soda.project.domain.Project;
import com.soda.project.domain.ProjectErrorCode;
import com.soda.project.domain.stage.Stage;
import com.soda.project.domain.stage.StageConstants;
import com.soda.project.domain.stage.StageErrorCode;
import com.soda.project.domain.stage.StageProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StageValidatorImpl implements StageValidator {
    private final StageProvider stageProvider;

    @Override
    public void validateStageName(Project project, String name, Long stageId) {
        boolean isDuplicate = stageProvider.existsByProjectAndNameAndIsDeletedFalseAndIdNot(project, name.trim(), stageId);
        if (isDuplicate) {
            log.warn("단계 이름 중복: projectId={}, name={}, stageId={}", project.getId(), name.trim(), stageId);
            throw new GeneralException(StageErrorCode.DUPLICATE_STAGE_NAME);
        }
    }

    @Override
    public float validateAndGetNewOrder(Project project, Long prevStageId, Long nextStageId) {
        Float prevOrder = null;
        Float nextOrder = null;

        if (prevStageId != null) {
            Stage prevStage = stageProvider.getStageOrThrow(prevStageId);
            validateStageProject(prevStage, project);
            prevOrder = prevStage.getStageOrder();
        }

        if (nextStageId != null) {
            Stage nextStage = stageProvider.getStageOrThrow(nextStageId);
            validateStageProject(nextStage, project);
            nextOrder = nextStage.getStageOrder();
        }

        if (prevOrder != null && nextOrder != null && prevOrder >= nextOrder) {
            log.error("단계 순서 검증 실패: projectId={}, prevStageId={}, nextStageId={}, prevOrder={}, nextOrder={}",
                    project.getId(), prevStageId, nextStageId, prevOrder, nextOrder);
            throw new GeneralException(StageErrorCode.INVALID_STAGE_ORDER);
        }
        return Stage.calculateNewOrder(prevOrder, nextOrder);
    }

    @Override
    public void validateStageCount(Project project) {
        int stageCount = stageProvider.countByProjectAndIsDeletedFalse(project);
        if (stageCount >= StageConstants.MAX_STAGES_PER_PROJECT) {
            log.error("단계 개수 초과: projectId={}, currentCount={}, maxCount={}",
                    project.getId(), stageCount, StageConstants.MAX_STAGES_PER_PROJECT);
            throw new GeneralException(StageErrorCode.STAGE_LIMIT_EXCEEDED);
        }
    }

    @Override
    public void validateStageProject(Stage stage, Project project) {
        if (!stage.getProject().equals(project)) {
            log.error("잘못된 단계-프로젝트 관계: stageId={}, stageProjectId={}, requestProjectId={}",
                    stage.getId(), stage.getProject().getId(), project.getId());
            throw new GeneralException(ProjectErrorCode.INVALID_STAGE_FOR_PROJECT);
        }
    }

}