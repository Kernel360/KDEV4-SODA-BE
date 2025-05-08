package com.soda.project.infrastructure.stage;

import com.soda.global.response.GeneralException;
import com.soda.project.domain.Project;
import com.soda.project.domain.ProjectErrorCode;
import com.soda.project.domain.stage.Stage;
import com.soda.project.domain.stage.StageConstants;
import com.soda.project.domain.stage.StageErrorCode;
import com.soda.project.domain.stage.StageProvider;
import com.soda.project.domain.stage.StageValidator;
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
        if (name == null || name.isBlank()) {
            log.error("단계 이름 검증 실패: 이름이 비어있음");
            throw new GeneralException(StageErrorCode.INVALID_STAGE_NAME);
        }

        if (name.length() > StageConstants.MAX_STAGE_NAME_LENGTH) {
            log.error("단계 이름 검증 실패: 이름 길이 초과 (최대 {}자)", StageConstants.MAX_STAGE_NAME_LENGTH);
            throw new GeneralException(StageErrorCode.INVALID_STAGE_NAME);
        }

        boolean isDuplicate = stageProvider.existsByProjectAndNameAndIsDeletedFalseAndIdNot(project, name, stageId);
        if (isDuplicate) {
            log.warn("단계 이름 검증 실패: 프로젝트 ID {} 내에 이미 '{}' 이름의 단계가 존재함", project.getId(), name);
            throw new GeneralException(StageErrorCode.DUPLICATE_STAGE_NAME);
        }
    }

    @Override
    public void validateStageOrder(Project project, Long prevStageId, Long nextStageId) {
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
            log.error("단계 순서 검증 실패: 이전 단계 순서({})가 다음 단계 순서({})보다 크거나 같음", prevOrder, nextOrder);
            throw new GeneralException(StageErrorCode.INVALID_STAGE_ORDER);
        }
    }

    @Override
    public void validateStageCount(Project project) {
        int stageCount = stageProvider.countByProjectAndIsDeletedFalse(project);
        if (stageCount >= StageConstants.MAX_STAGES_PER_PROJECT) {
            throw new GeneralException(StageErrorCode.STAGE_LIMIT_EXCEEDED);
        }
    }

    @Override
    public void validateStageProject(Stage stage, Project project) {
        if (!stage.getProject().equals(project)) {
            throw new GeneralException(ProjectErrorCode.INVALID_STAGE_FOR_PROJECT);
        }
    }

    @Override
    public Stage validateStage(Long stageId, Project project) {
        Stage stage = stageProvider.findById(stageId).orElseThrow(
                () -> new GeneralException(StageErrorCode.STAGE_NOT_FOUND));

        validateStageProject(stage, project);
        return stage;
    }
}