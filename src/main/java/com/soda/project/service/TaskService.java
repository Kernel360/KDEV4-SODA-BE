package com.soda.project.service;

import com.soda.global.response.GeneralException;
import com.soda.project.domain.task.TaskCreateRequest;
import com.soda.project.domain.task.TaskReadResponse;
import com.soda.project.domain.task.TaskResponse;
import com.soda.project.entity.Stage;
import com.soda.project.entity.Task;
import com.soda.project.error.StageErrorCode;
import com.soda.project.error.TaskErrorCode;
import com.soda.project.repository.StageRepository;
import com.soda.project.repository.TaskRepository;
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
public class TaskService {

    private final TaskRepository taskRepository;
    private final StageRepository stageRepository;

    private static final float TASK_ORDER_INCREMENT = 1.0f;
    private static final float TASK_INITIAL_ORDER = 1.0f;

    /**
     * 새로운 태스크를 특정 스테이지에 추가합니다.
     * 태스크 순서는 요청의 `prevTaskId`, `nextTaskId`를 기반으로 서버에서 계산됩니다.
     * 스테이지 정보는 StageRepository를 통해 직접 조회합니다.
     *
     * @param request 태스크 생성 요청 정보 (stageId, title, content, prevTaskId, nextTaskId 포함)
     * @return 생성된 태스크 정보 DTO (`TaskResponse`)
     * @throws GeneralException 스테이지(`StageErrorCode.STAGE_NOT_FOUND`) 또는 참조된 이전/다음 태스크(`TaskErrorCode.TASK_NOT_FOUND`)를 찾을 수 없거나,
     *                          참조된 태스크가 다른 스테이지 소속(`TaskErrorCode.TASK_STAGE_MISMATCH`)이거나,
     *                          순서 설정이 유효하지 않은(`TaskErrorCode.INVALID_TASK_ORDER`) 경우 발생합니다.
     */
    @Transactional
    public TaskResponse addTask(TaskCreateRequest request) {
        // 1. 대상 스테이지 조회 (StageRepository 사용)
        Stage stage = stageRepository.findByIdAndIsDeletedFalse(request.getStageId())
                .orElseThrow(() -> {
                    log.warn("태스크 추가 실패: 스테이지 ID {} 를 찾을 수 없음", request.getStageId());
                    return new GeneralException(StageErrorCode.STAGE_NOT_FOUND);
                });

        // 2. (선택 사항) 스테이지 당 최대 태스크 개수 제한 확인

        // 3. 새 태스크 순서 계산
        float newTaskOrder = calculateNewTaskOrder(stage, request.getPrevTaskId(), request.getNextTaskId());

        // 4. 새 태스크 엔티티 생성 및 저장
        Task task = Task.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .stage(stage)
                .taskOrder(newTaskOrder)
                .build();
        Task savedTask = taskRepository.save(task);

        log.info("태스크 추가 성공: 스테이지 ID {}, 새 태스크 ID {}, 순서 {}",
                stage.getId(), savedTask.getId(), savedTask.getTaskOrder());
        return TaskResponse.fromEntity(savedTask);
    }

    /**
     * 특정 스테이지에 속한 모든 활성(삭제되지 않은) 태스크를 순서대로 조회합니다.
     * 스테이지 존재 여부는 StageRepository를 통해 직접 확인합니다.
     *
     * @param stageId 태스크를 조회할 스테이지의 ID
     * @return 해당 스테이지의 태스크 DTO 목록 (`List<TaskReadResponse>`)
     * @throws GeneralException 스테이지(`StageErrorCode.STAGE_NOT_FOUND`)를 찾을 수 없는 경우 발생합니다.
     */
    public List<TaskReadResponse> getTasksByStage(Long stageId) {
        if (!stageRepository.existsByIdAndIsDeletedFalse(stageId)) {
            log.warn("태스크 조회 실패: 스테이지 ID {} 를 찾을 수 없음", stageId);
            throw new GeneralException(StageErrorCode.STAGE_NOT_FOUND);
        }

        List<Task> tasks = taskRepository.findByStageIdAndIsDeletedFalseOrderByTaskOrderAsc(stageId);
        log.info("스테이지 ID {} 의 활성 태스크 {}개 조회 성공", stageId, tasks.size());

        return tasks.stream()
                .map(TaskReadResponse::fromEntity)
                .collect(Collectors.toList());
    }


    // --- Private Helper Methods ---

    /**
     * ID로 활성(삭제되지 않은) 태스크를 찾아 반환합니다. 없으면 예외를 발생시킵니다.
     * (TaskRepository에 findByIdAndIsDeletedFalse 메서드가 필요합니다)
     */
    private Task findActiveTaskByIdOrThrow(Long taskId) {
        return taskRepository.findByIdAndIsDeletedFalse(taskId)
                .orElseThrow(() -> {
                    log.warn("활성 태스크 조회/수정/삭제 실패: ID {} 를 찾을 수 없음", taskId);
                    return new GeneralException(TaskErrorCode.TASK_NOT_FOUND);
                });
    }

    /**
     * (내부 사용) 이전/다음 태스크 ID를 기반으로 새 태스크의 순서를 계산합니다.
     * (이 메서드는 Stage 엔티티를 파라미터로 받으므로 StageRepository 직접 접근 불필요)
     */
    private float calculateNewTaskOrder(Stage stage, Long prevTaskId, Long nextTaskId) {
        Float prevOrder = null;
        Float nextOrder = null;
        Long stageId = stage.getId();

        if (prevTaskId != null) {
            Task prevTask = findActiveTaskByIdOrThrow(prevTaskId);
            if (!prevTask.getStage().getId().equals(stageId)) {
                log.error("태스크 순서 계산 실패: 이전 태스크 ID {} 가 스테이지 ID {} 에 속하지 않음", prevTaskId, stageId);
                throw new GeneralException(TaskErrorCode.TASK_STAGE_MISMATCH);
            }
            prevOrder = prevTask.getTaskOrder();
        }

        if (nextTaskId != null) {
            Task nextTask = findActiveTaskByIdOrThrow(nextTaskId);
            if (!nextTask.getStage().getId().equals(stageId)) {
                log.error("태스크 순서 계산 실패: 다음 태스크 ID {} 가 스테이지 ID {} 에 속하지 않음", nextTaskId, stageId);
                throw new GeneralException(TaskErrorCode.TASK_STAGE_MISMATCH);
            }
            nextOrder = nextTask.getTaskOrder();
        }

        if (prevOrder != null && nextOrder != null) {
            if (prevOrder >= nextOrder) {
                log.error("태스크 순서 계산 실패: 이전 태스크 순서({}) >= 다음 태스크 순서({})", prevOrder, nextOrder);
                throw new GeneralException(TaskErrorCode.INVALID_TASK_ORDER);
            }
            return (prevOrder + nextOrder) / 2.0f;
        } else if (prevOrder != null) {
            return prevOrder + TASK_ORDER_INCREMENT;
        } else if (nextOrder != null) {
            if (nextOrder <= 0) {
                log.warn("다음 태스크 순서({})가 0 이하입니다. 순서를 {}로 설정합니다.", nextOrder, nextOrder / 2.0f);
            }
            return nextOrder / 2.0f;
        } else {
            log.info("스테이지 ID {} 에 첫 태스크 추가. 초기 순서 {} 적용", stageId, TASK_INITIAL_ORDER);
            return TASK_INITIAL_ORDER;
        }
    }
}