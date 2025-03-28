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

    // 초기 단계 이름 목록 정의
    private static final List<String> INITIAL_STAGE_NAMES = Arrays.asList(
            "요구사항 정의", "화면 설계", "디자인", "퍼블리싱", "개발", "검수"
    );

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

    /**
     * 특정 단계의 순서를 변경하는 메서드
     *
     * @param stageId 순서를 변경할 단계의 ID
     * @param request 새로운 순서 값을 포함하는 요청 객체 (newOrder 필드 사용)
     * @throws GeneralException 단계를 찾을 수 없는 경우 발생
     */
    @Transactional
    public void moveStage(Long stageId, StageMoveRequest request) {
        Stage stage = stageRepository.findById(stageId)
                .orElseThrow(() -> {
                    log.error("단계 이동 실패: 단계 ID {} 를 찾을 수 없음", stageId);
                    return new GeneralException(StageErrorCode.STAGE_NOT_FOUND);
                });
        log.info("단계 이동 요청: 단계 ID {}, 이전 순서 {}, 새 순서 {}", stageId, stage.getStageOrder(), request.getNewOrder());
        stage.moveStageOrder(request.getNewOrder());
        stageRepository.save(stage);
    }

    /**
     * 특정 단계를 (논리적으로) 삭제하는 메서드
     * 실제 데이터베이스에서 행을 지우는 것이 아니라, isDeleted 같은 플래그를 변경합니다.
     *
     * @param stageId 삭제할 단계의 ID
     * @throws GeneralException 단계를 찾을 수 없는 경우 발생
     */
    @Transactional
    public void deleteStage(Long stageId) {
        Stage stage = stageRepository.findById(stageId)
                .orElseThrow(() -> {
                    log.error("단계 삭제 실패: 단계 ID {} 를 찾을 수 없음", stageId);
                    return new GeneralException(StageErrorCode.STAGE_NOT_FOUND);
                });
        stage.delete();
        log.info("단계 삭제 성공: 단계 ID {}", stageId);
        stageRepository.save(stage);
    }

    /**
     * 특정 프로젝트에 미리 정의된 초기 단계들을 생성하는 메서드
     * (예: 요구사항 정의, 화면 설계 등)
     *
     * @param projectId 초기 단계를 생성할 프로젝트의 ID
     * @return 생성된 초기 단계들의 DTO 리스트 (StageReadResponse 형태)
     * @throws GeneralException 프로젝트를 찾을 수 없는 경우 발생
     */
    @Transactional
    public List<StageReadResponse> createInitialStages(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> {
                    log.error("초기 단계 생성 실패: 프로젝트 ID {} 를 찾을 수 없음", projectId);
                    return new GeneralException(ProjectErrorCode.PROJECT_NOT_FOUND);
                });

        List<Stage> initialStages = createStages(project);
        stageRepository.saveAll(initialStages);
        log.info("초기 단계 생성 성공: 프로젝트 ID {}, {}개 단계 생성됨", projectId, initialStages.size());

        return initialStages.stream()
                .map(StageReadResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 주어진 프로젝트에 대해 초기 단계 엔티티 리스트를 생성하는 내부 헬퍼 메서드
     *
     * @param project 단계를 생성할 프로젝트 엔티티
     * @return 생성된 Stage 엔티티 리스트 (아직 DB에 저장되지 않은 상태)
     */
    private List<Stage> createStages(Project project) {
        List<Stage> stages = new ArrayList<>();
        float order = 1.0f;

        for (String name : INITIAL_STAGE_NAMES) {
            Stage stage = Stage.builder()
                    .project(project)
                    .name(name)
                    .stageOrder(order)
                    .build();
            stages.add(stage);
            order += 1.0f;
        }

        return stages;
    }
}