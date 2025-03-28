package com.soda.project.service;

import com.soda.global.response.GeneralException;
import com.soda.project.domain.stage.StageCreateRequest;
import com.soda.project.domain.stage.StageMoveRequest;
import com.soda.project.domain.stage.StageReadResponse;
import com.soda.project.domain.stage.StageResponse;
import com.soda.project.entity.Project;
import com.soda.project.entity.Stage;
import com.soda.project.error.ProjectErrorCode;
import com.soda.project.error.StageErrorCode;
import com.soda.project.repository.ProjectRepository;
import com.soda.project.repository.StageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StageService {

    private final StageRepository stageRepository;
    private final ProjectRepository projectRepository;


    /**
     * 새로운 단계를 프로젝트에 추가하는 메서드
     * 프로젝트의 최대 단계 개수(10개)를 초과할 수 없습니다.
     *
     * @param request 단계 생성 요청 정보 (projectId, name, newOrder 포함)
     * @return 생성된 단계 정보 DTO
     * @throws GeneralException 프로젝트를 찾을 수 없거나 단계 개수 제한(10개)을 초과한 경우 발생
     */
    @Transactional
    public StageResponse addStage(StageCreateRequest request) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> {
                    log.error("단계 추가 실패: 프로젝트 ID {} 를 찾을 수 없음", request.getProjectId());
                    return new GeneralException(ProjectErrorCode.PROJECT_NOT_FOUND);
                });


        int currentActiveStageCount = stageRepository.countByProjectAndIsDeletedFalse(project);

        if (currentActiveStageCount >= 10) {
            log.warn("단계 추가 실패: 프로젝트 ID {} 의 활성 단계 개수 제한(10개) 초과 (현재 {}개)", project.getId(), currentActiveStageCount);
            throw new GeneralException(StageErrorCode.STAGE_LIMIT_EXCEEDED);
        }

        Stage stage = Stage.builder()
                .stageOrder(request.getNewOrder())
                .name(request.getName())
                .project(project)
                .build();
        Stage savedStage = stageRepository.save(stage);
        log.info("단계 추가 성공: 프로젝트 ID {}, 새 단계 ID {}", project.getId(), savedStage.getId());
        return StageResponse.fromEntity(savedStage);
    }

    /**
     * 특정 프로젝트의 모든 단계(삭제되지 않은)를 순서대로 조회하는 메서드
     *
     * @param projectId 프로젝트 ID
     * @return 해당 프로젝트의 단계 목록 DTO 리스트 (StageReadResponse 형태)
     */

    public List<StageReadResponse> getStages(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            log.warn("단계 조회 실패: 프로젝트 ID {} 를 찾을 수 없음", projectId);
            throw new GeneralException(ProjectErrorCode.PROJECT_NOT_FOUND);
        }
        List<Stage> stages = stageRepository.findByProjectIdAndIsDeletedFalseOrderByStageOrderAsc(projectId);
        log.info("프로젝트 ID {} 의 단계 {}개 조회 성공", projectId, stages.size());

        return stages.stream()
                .map(StageReadResponse::fromEntity)
                .collect(Collectors.toList());
    }
}