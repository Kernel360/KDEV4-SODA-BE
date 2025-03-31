package com.soda.project.controller;

import com.soda.global.response.ApiResponseForm;
import com.soda.project.domain.task.TaskCreateRequest;
import com.soda.project.domain.task.TaskResponse;
import com.soda.project.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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


}
