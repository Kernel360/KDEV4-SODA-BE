package com.soda.project.service;

import com.soda.global.log.dataLog.annotation.LoggableEntityAction;
import com.soda.global.response.CommonErrorCode;
import com.soda.global.response.GeneralException;
import com.soda.project.domain.stage.StageCreateRequest;
import com.soda.project.domain.stage.StageMoveRequest;
import com.soda.project.domain.stage.StageReadResponse;
import com.soda.project.domain.stage.StageResponse;
import com.soda.project.domain.stage.StageUpdateRequest;
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

    private final TaskService taskService;

    private static final float ORDER_INCREMENT = 1.0f;
    private static final float INITIAL_ORDER = 1.0f;
    private static final List<String> INITIAL_STAGE_NAMES = Arrays.asList(
            "요구사항 정의", "화면 설계", "디자인", "퍼블리싱", "개발", "검수"
    );

    /**
     * 새로운 단계를 프로젝트에 추가합니다.
     * 단계 순서는 요청에 포함된 이전/다음 단계 ID(`prevStageId`, `nextStageId`)를 기반으로 서버에서 계산됩니다.
     * 프로젝트의 활성 단계는 최대 10개까지 가능합니다.
     *
     * @param request 단계 생성 요청 정보 (projectId, name, prevStageId, nextStageId 포함)
     * @return 생성된 단계 정보 DTO (`StageResponse`)
     * @throws GeneralException 프로젝트(`ProjectErrorCode.PROJECT_NOT_FOUND`) 또는 참조된 이전/다음 단계(`StageErrorCode.STAGE_NOT_FOUND`)를 찾을 수 없거나,
     *                          단계 개수 제한(`StageErrorCode.STAGE_LIMIT_EXCEEDED`)을 초과했거나,
     *                          참조된 단계가 다른 프로젝트 소속(`StageErrorCode.STAGE_PROJECT_MISMATCH`)이거나,
     *                          순서 설정이 유효하지 않은(`StageErrorCode.INVALID_STAGE_ORDER`) 경우 발생합니다.
     */
    @LoggableEntityAction(action = "CREATE", entityClass = Stage.class)
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

        float newOrder = calculateNewOrder(project, request.getPrevStageId(), request.getNextStageId());

        Stage stage = Stage.builder()
                .stageOrder(newOrder)
                .name(request.getName())
                .project(project)
                .build();
        Stage savedStage = stageRepository.save(stage);

        log.info("단계 추가 성공: 프로젝트 ID {}, 새 단계 ID {}, 순서 {}",
                project.getId(), savedStage.getId(), savedStage.getStageOrder());
        return StageResponse.fromEntity(savedStage);
    }

    /**
     * 특정 프로젝트의 모든 활성(삭제되지 않은) 단계를 순서(`stageOrder` 오름차순)대로 조회합니다.
     *
     * @param projectId 단계를 조회할 프로젝트의 ID
     * @return 해당 프로젝트의 단계 DTO 목록 (`List<StageReadResponse>`), 순서대로 정렬됨. 활성 단계가 없으면 빈 리스트 반환.
     * @throws GeneralException 프로젝트(`ProjectErrorCode.PROJECT_NOT_FOUND`)를 찾을 수 없는 경우 발생합니다.
     */
    public List<StageReadResponse> getStages(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            log.warn("단계 조회 실패: 프로젝트 ID {} 를 찾을 수 없음", projectId);
            throw new GeneralException(ProjectErrorCode.PROJECT_NOT_FOUND);
        }
        List<Stage> stages = stageRepository.findByProjectIdAndIsDeletedFalseOrderByStageOrderAsc(projectId);
        log.info("프로젝트 ID {} 의 활성 단계 {}개 조회 성공", projectId, stages.size());

        return stages.stream()
                .map(StageReadResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 특정 단계의 순서(위치)를 변경합니다.
     * 새로운 위치는 요청의 `prevStageId`와 `nextStageId`에 의해 정의되며, 실제 순서 값은 서버에서 계산됩니다.
     *
     * @param stageId 순서를 변경할 단계의 ID
     * @param request 새로운 위치를 정의하는 요청 정보 (prevStageId, nextStageId 포함)
     * @throws GeneralException 이동할 단계(`StageErrorCode.STAGE_NOT_FOUND`) 또는 참조된 이전/다음 단계(`StageErrorCode.STAGE_NOT_FOUND`)를 찾을 수 없거나,
     *                          참조된 단계가 다른 프로젝트 소속(`StageErrorCode.STAGE_PROJECT_MISMATCH`)이거나,
     *                          순서 설정이 유효하지 않은(`StageErrorCode.INVALID_STAGE_ORDER`) 경우 발생합니다.
     *                          (이동할 단계에 프로젝트 정보가 없는 비정상 상태 포함)
     */
    @LoggableEntityAction(action = "UPDATE", entityClass = Stage.class)
    @Transactional
    public void moveStage(Long stageId, StageMoveRequest request) {
        Stage stageToMove = stageRepository.findById(stageId)
                .orElseThrow(() -> {
                    log.error("단계 이동 실패: 이동할 단계 ID {} 를 찾을 수 없음", stageId);
                    return new GeneralException(StageErrorCode.STAGE_NOT_FOUND);
                });

        Project project = stageToMove.getProject();
        if (project == null) {
            log.error("단계 이동 실패: 단계 ID {} 에 연결된 프로젝트 정보가 없습니다.", stageId);
            throw new GeneralException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }

        float newOrder = calculateNewOrder(project, request.getPrevStageId(), request.getNextStageId());

        log.info("단계 이동 실행: 단계 ID {}, 이전 순서 {}, 새 순서 {}",
                stageId, stageToMove.getStageOrder(), newOrder);
        stageToMove.moveStageOrder(newOrder);

         stageRepository.save(stageToMove);
    }

    /**
     * 특정 단계를 논리적으로 삭제합니다.
     * 실제 DB 레코드를 삭제하는 대신, `isDeleted` 와 같은 플래그를 true로 설정합니다.
     *
     * @param stageId 논리적으로 삭제할 단계의 ID
     * @throws GeneralException 삭제할 단계(`StageErrorCode.STAGE_NOT_FOUND`)를 찾을 수 없는 경우 발생합니다.
     */
    @LoggableEntityAction(action = "DELETE", entityClass = Stage.class)
    @Transactional
    public void deleteStage(Long stageId) {
        Stage stage = stageRepository.findById(stageId)
                .orElseThrow(() -> {
                    log.error("단계 삭제 실패: 단계 ID {} 를 찾을 수 없음", stageId);
                    return new GeneralException(StageErrorCode.STAGE_NOT_FOUND);
                });

        stage.delete();

        log.info("단계 삭제 성공 (논리적): 단계 ID {}", stageId);
         stageRepository.save(stage);
    }

    public Stage validateStage(Long stageId, Project project) {
        Stage stage = stageRepository.findById(stageId).orElseThrow(
                ()-> new GeneralException(StageErrorCode.STAGE_NOT_FOUND)
        );

        if (!stage.getProject().equals(project)) {
            throw new GeneralException(ProjectErrorCode.INVALID_STAGE_FOR_PROJECT);
        }

        return stage;
    }

    /**
     * 특정 프로젝트에 미리 정의된 초기 단계들을 일괄 생성합니다.
     * (예: "요구사항 정의", "화면 설계" 등)
     *
     * @param projectId 초기 단계를 생성할 프로젝트의 ID
     * @return 생성된 초기 단계들의 DTO 목록 (`List<StageReadResponse>`)
     * @throws GeneralException 프로젝트(`ProjectErrorCode.PROJECT_NOT_FOUND`)를 찾을 수 없는 경우 발생합니다.
     */
    @LoggableEntityAction(action = "CREATE", entityClass = Stage.class)
    @Transactional
    public List<StageReadResponse> createInitialStages(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> {
                    log.error("초기 단계 생성 실패: 프로젝트 ID {} 를 찾을 수 없음", projectId);
                    return new GeneralException(ProjectErrorCode.PROJECT_NOT_FOUND);
                });

        List<Stage> initialStages = createStagesInternal(project);
        stageRepository.saveAll(initialStages);
        log.info("초기 단계 생성 성공: 프로젝트 ID {}, {}개 단계 생성됨", projectId, initialStages.size());

        return initialStages.stream()
                .map(StageReadResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     *  주어진 프로젝트에 대해 초기 단계 엔티티 리스트를 생성합니다.
     * @param project 단계를 생성할 프로젝트 엔티티
     * @return 생성된 Stage 엔티티 리스트 (DB 저장 전 상태)
     */
    private List<Stage> createStagesInternal(Project project) {
        List<Stage> stages = new ArrayList<>();
        float order = INITIAL_ORDER;

        for (String name : INITIAL_STAGE_NAMES) {
            Stage stage = Stage.builder()
                    .project(project)
                    .name(name)
                    .stageOrder(order)
                    .build();
            stages.add(stage);
            order += ORDER_INCREMENT;
        }
        return stages;
    }

    /**
     *  이전/다음 단계 ID를 기반으로 새 단계의 순서(`stageOrder`)를 계산합니다.
     * @param project     단계가 속할 프로젝트
     * @param prevStageId 이전 단계 ID (없으면 null)
     * @param nextStageId 다음 단계 ID (없으면 null)
     * @return 계산된 새 단계의 순서값 (float)
     * @throws GeneralException 참조된 단계 ID가 존재하지 않거나(`STAGE_NOT_FOUND`),
     *                          해당 프로젝트 소속이 아니거나(`STAGE_PROJECT_MISMATCH`),
     *                          순서 설정이 유효하지 않은 경우(`INVALID_STAGE_ORDER`) 발생
     */
    private float calculateNewOrder(Project project, Long prevStageId, Long nextStageId) {
        Float prevOrder = null;
        Float nextOrder = null;

        if (prevStageId != null) {
            Stage prevStage = stageRepository.findById(prevStageId)
                    .orElseThrow(() -> {
                        log.error("단계 순서 계산 실패: 이전 단계 ID {} 를 찾을 수 없음", prevStageId);
                        return new GeneralException(StageErrorCode.STAGE_NOT_FOUND);
                    });
            if (!prevStage.getProject().getId().equals(project.getId())) {
                log.error("단계 순서 계산 실패: 이전 단계 ID {} 가 프로젝트 ID {} 에 속하지 않음", prevStageId, project.getId());
                throw new GeneralException(StageErrorCode.STAGE_PROJECT_MISMATCH);
            }
            prevOrder = prevStage.getStageOrder();
        }

        if (nextStageId != null) {
            Stage nextStage = stageRepository.findById(nextStageId)
                    .orElseThrow(() -> {
                        log.error("단계 순서 계산 실패: 다음 단계 ID {} 를 찾을 수 없음", nextStageId);
                        return new GeneralException(StageErrorCode.STAGE_NOT_FOUND);
                    });
            if (!nextStage.getProject().getId().equals(project.getId())) {
                log.error("단계 순서 계산 실패: 다음 단계 ID {} 가 프로젝트 ID {} 에 속하지 않음", nextStageId, project.getId());
                throw new GeneralException(StageErrorCode.STAGE_PROJECT_MISMATCH);
            }
            nextOrder = nextStage.getStageOrder();
        }

        if (prevOrder != null && nextOrder != null) {
            if (prevOrder >= nextOrder) {
                log.error("단계 순서 계산 실패: 이전 단계 순서({}) >= 다음 단계 순서({})", prevOrder, nextOrder);
                throw new GeneralException(StageErrorCode.INVALID_STAGE_ORDER);
            }
            return (prevOrder + nextOrder) / 2.0f;
        } else if (prevOrder != null) {
            return prevOrder + ORDER_INCREMENT;
        } else if (nextOrder != null) {
            if (nextOrder <= 0) {
                log.warn("다음 단계 순서({})가 0 이하입니다. 순서를 {}로 설정합니다.", nextOrder, nextOrder / 2.0f);
            }
            return nextOrder / 2.0f;
        } else {
            log.info("프로젝트 ID {} 에 첫 단계 추가 또는 이동. 초기 순서 {} 적용", project.getId(), INITIAL_ORDER);
            return INITIAL_ORDER;
        }
    }

    /**
     * 특정 단계의 정보를 수정합니다. (현재는 이름만 수정 가능)
     *
     * @param stageId 수정할 단계의 ID
     * @param request 수정할 정보 DTO (새로운 이름 포함)
     * @return 수정된 단계 정보 DTO (`StageResponse`)
     * @throws GeneralException 수정할 단계(`StageErrorCode.STAGE_NOT_FOUND`)를 찾을 수 없거나,
     *                          같은 프로젝트 내에 동일한 이름의 다른 활성 단계가 이미 존재(`StageErrorCode.DUPLICATE_STAGE_NAME`)하는 경우 발생합니다.
     *                          (단계에 프로젝트 정보가 없는 비정상 상태 포함)
     */
    @LoggableEntityAction(action = "UPDATE", entityClass = Stage.class)
    @Transactional
    public StageResponse updateStage(Long stageId, StageUpdateRequest request) {
        Stage stageToUpdate = stageRepository.findById(stageId)
                .orElseThrow(() -> {
                    log.error("단계 수정 실패: 단계 ID {} 를 찾을 수 없음", stageId);
                    return new GeneralException(StageErrorCode.STAGE_NOT_FOUND);
                });

        Project project = stageToUpdate.getProject();
        if (project == null) {
            log.error("단계 수정 실패: 단계 ID {} 에 연결된 프로젝트 정보가 없습니다.", stageId);
            throw new GeneralException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }

        String newName = request.getName();
        String currentName = stageToUpdate.getName();

        if (!newName.equals(currentName)) {
            log.info("단계 이름 변경 감지: stageId={}, oldName='{}', newName='{}'", stageId, currentName, newName);
            boolean isDuplicate = stageRepository.existsByProjectAndNameAndIsDeletedFalseAndIdNot(project, newName, stageId);
            if (isDuplicate) {
                log.warn("단계 수정 실패: 프로젝트 ID {} 내에 이미 '{}' 이름의 단계가 존재함 (stageId {} 제외)", project.getId(), newName, stageId);
                throw new GeneralException(StageErrorCode.DUPLICATE_STAGE_NAME);
            }
            stageToUpdate.updateName(newName);
            log.info("단계 이름 업데이트 실행: stageId={}", stageId);
        } else {
            log.info("단계 이름 변경 없음 (동일한 이름 요청): stageId={}, name='{}'", stageId, newName);
        }

         Stage updatedStage = stageRepository.save(stageToUpdate);

        return StageResponse.fromEntity(stageToUpdate);
    }

    public Stage findById(Long stageId) {
        return stageRepository.findById(stageId)
                .orElseThrow(() -> new GeneralException(StageErrorCode.STAGE_NOT_FOUND));
    }
}