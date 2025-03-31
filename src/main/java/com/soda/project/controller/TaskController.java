package com.soda.project.controller;

import com.soda.global.response.ApiResponseForm;
import com.soda.project.domain.task.TaskCreateRequest;
import com.soda.project.domain.task.TaskReadResponse;
import com.soda.project.domain.task.TaskResponse;
import com.soda.project.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    /**
     * 새로운 테스크 추가 API
     *
     * @param request 테스크 생성 요청 바디
     * @return 생성된 테스크 정보
     */
    @PostMapping
    public ResponseEntity<ApiResponseForm<TaskResponse>> addTask(@RequestBody TaskCreateRequest request) {
        TaskResponse task = taskService.addTask(request);
        return ResponseEntity.ok(ApiResponseForm.success(task, "테스크 추가 성공"));
    }

    /**
     * 특정 스테이지의 모든 테스크 조회 API
     * @param stageId 테스크를 조회할 스테이지 ID (경로 변수)
     * @return 해당 스테이지의 테스크 목록
     */
    @GetMapping("/stage/{stageId}")
    public ResponseEntity<ApiResponseForm<List<TaskReadResponse>>> getTasksByStage(@PathVariable Long stageId) {
        List<TaskReadResponse> tasks = taskService.getTasksByStage(stageId);
        return ResponseEntity.ok(ApiResponseForm.success(tasks, "스테이지 테스크 목록 조회 성공"));
    }

}
