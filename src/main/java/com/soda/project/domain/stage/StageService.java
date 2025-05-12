package com.soda.project.domain.stage;

import com.soda.global.response.GeneralException;
import com.soda.project.domain.Project;
import com.soda.project.interfaces.stage.dto.StageReadResponse;
import com.soda.project.interfaces.stage.dto.StageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StageService {
    private final StageProvider stageProvider;

    public Stage getStageOrThrow(Long stageId) {
        return stageProvider.getStageOrThrow(stageId);
    }

    public StageResponse createStage(Project project, String stageName, Float newOrder) {
        Stage newStage = Stage.createStage(project, stageName.trim(), newOrder);
        Stage savedStage = stageProvider.store(newStage);

        log.info("새 단계 생성 완료: stageId={}, projectId={}, name={}, order={}",
                savedStage.getId(), project.getId(), savedStage.getName(), savedStage.getStageOrder());
        return StageResponse.fromEntity(savedStage);
    }

    public List<StageReadResponse> getStagesByProjectId(Long projectId) {
        stageProvider.getProjectOrThrow(projectId);
        List<Stage> stages = stageProvider.findByProjectIdAndIsDeletedFalseOrderByStageOrderAsc(projectId);
        log.info("프로젝트 ID {} 의 활성 단계 {}개 조회 성공", projectId, stages.size());

        return stages.stream()
                .map(StageReadResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public StageResponse updateStage(Stage stage, String name) {
        stage.updateName(name);
        Stage updatedStage = stageProvider.store(stage);
        return StageResponse.fromEntity(updatedStage);
    }

    public void moveStage(Stage stage, float newOrder) {
        log.info("단계 이동 실행: stageId={}, oldOrder={}, newOrder={}",
                stage.getId(), stage.getStageOrder(), newOrder);
        stage.moveStageOrder(newOrder);
        stageProvider.store(stage);
    }

    public void deleteStage(Long stageId) {
        Stage stage = stageProvider.getStageOrThrow(stageId);
        Project project = stage.getProject();
        stage.delete();
        stageProvider.store(stage);
        log.info("단계 삭제 완료: stageId={}, projectId={}", stageId, project.getId());
    }

    public Stage validateStage(Long stageId, Project project) {
        return stageProvider.findById(stageId).orElseThrow(
                () -> {
                    log.error("단계를 찾을 수 없음: stageId={}, projectId={}", stageId, project.getId());
                    return new GeneralException(StageErrorCode.STAGE_NOT_FOUND);
                });
    }

}
