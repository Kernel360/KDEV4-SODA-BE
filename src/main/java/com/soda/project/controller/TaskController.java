package com.soda.project.controller;

import com.soda.global.response.ApiResponseForm;
import com.soda.project.domain.task.*;
import com.soda.project.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    /**
     * 새로운 태스크 추가 API
     *
     * @param request 태스크 생성 요청 바디
     * @return 생성된 태스크 정보
     */
    @PostMapping("/tasks")
    public ResponseEntity<ApiResponseForm<TaskResponse>> addTask(@RequestBody TaskCreateRequest request) {
        TaskResponse task = taskService.addTask(request);
        return ResponseEntity.ok(ApiResponseForm.success(task, "태스크 추가 성공"));
    }

    /**
     * 특정 스테이지의 모든 태스크 조회 API
     * @param stageId 태스크를 조회할 스테이지 ID (경로 변수)
     * @return 해당 스테이지의 태스크 목록
     */
    @GetMapping("/stages/{stageId}/tasks")
    public ResponseEntity<ApiResponseForm<List<TaskReadResponse>>> getTasksByStage(@PathVariable Long stageId) {
        List<TaskReadResponse> tasks = taskService.getTasksByStage(stageId);
        return ResponseEntity.ok(ApiResponseForm.success(tasks, "스테이지 태스크 목록 조회 성공"));
    }

    /**
     * 태스크 정보 수정 API (제목, 내용)
     * @param taskId 수정할 태스크 ID (경로 변수)
     * @param request 수정할 내용 DTOsadklfj
     * @return 수정된 태스크 정보
     */
    @PatchMapping("/tasks/{taskId}")
    public ResponseEntity<ApiResponseForm<TaskResponse>> updateTask(
            @PathVariable Long taskId,
            @RequestBody TaskUpdateRequest request) {
        TaskResponse updatedTask = taskService.updateTask(taskId, request);
        return ResponseEntity.ok(ApiResponseForm.success(updatedTask, "태스크 정보 수정 성공"));
    }

    /**
     * 태스크 순서 변경 API
     * @param taskId 순서를 변경할 태스크 ID (경로 변수)
     * @param request 새로운 위치 정보 DTO
     * @return 성공 응답 (내용 없음)
     */
    @PostMapping("/tasks/{taskId}/move")
    public ResponseEntity<ApiResponseForm<Void>> moveTask(
            @PathVariable Long taskId,
            @RequestBody TaskMoveRequest request) {
        taskService.moveTask(taskId, request);
        return ResponseEntity.ok(ApiResponseForm.success(null, "태스크 순서 변경 성공"));
    }

    /**
     * 태스크 삭제 API (논리적 삭제)
     * @param taskId 삭제할 태스크 ID (경로 변수)
     * @return 성공 응답 (내용 없음)
     */
    @DeleteMapping("/tasks/{taskId}")
    public ResponseEntity<ApiResponseForm<Void>> deleteTask(@PathVariable Long taskId) {
        taskService.deleteTask(taskId);
        return ResponseEntity.ok(ApiResponseForm.success(null, "태스크 삭제 성공"));
    }

}
