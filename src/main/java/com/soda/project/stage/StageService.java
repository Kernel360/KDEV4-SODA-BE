package com.soda.project.stage;

import com.soda.global.response.CommonErrorCode;
import com.soda.global.response.GeneralException;
import com.soda.project.Project;
import com.soda.project.ProjectErrorCode;
import com.soda.project.ProjectRepository;

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

    private final StageRepository stageRepository;
    private final ProjectRepository projectRepository;

    public Stage findById(Long stageId) {
        return stageRepository.findById(stageId)
                .orElseThrow(() -> new GeneralException(StageErrorCode.STAGE_NOT_FOUND));
    }

    @Transactional
    public StageResponse addStage(StageCreateRequest request) {
        Project project = projectRepository.findById(request.getProjectId())
                                           .orElseThrow(() -> {
                                               log.error("단계 추가 실패: 프로젝트 ID {} 를 찾을 수 없음", request.getProjectId());
                                               return new GeneralException(ProjectErrorCode.PROJECT_NOT_FOUND);
                                           });

        return project.addStage(request);
    }

    public List<StageReadResponse> getStages(Long projectId) {
        var project = projectRepository.findById(projectId).orElseThrow();

        return project.getActiveStages().stream()
                      .map(StageReadResponse::fromEntity)
                      .toList();
    }

    @Transactional
    public void moveStage(Long projectId, Long stageId, Long prevStageId, Long nextStageId) {
        var project = projectRepository.findById(projectId).orElseThrow();

        project.moveStage(stageId, prevStageId, nextStageId);
    }

    @Transactional
    public void deleteStage(Long projectId, Long stageId) {
        var project = projectRepository.findById(projectId).orElseThrow();

        project.deleteStage(stageId);

        log.info("단계 삭제 성공 (논리적): 단계 ID {}", stageId);
        projectRepository.save(project);
    }
}