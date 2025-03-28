package com.soda.project.controller;

import com.soda.global.response.ApiResponseForm;
import com.soda.project.domain.stage.StageCreateRequest;
import com.soda.project.domain.stage.StageMoveRequest;
import com.soda.project.domain.stage.StageReadResponse;
import com.soda.project.domain.stage.StageResponse;
import com.soda.project.service.StageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/stages")
@RestController
@RequiredArgsConstructor
public class StageController {

    private final StageService stageService;

    @PostMapping("")
    public ResponseEntity<ApiResponseForm<StageResponse>> addStage(@RequestBody StageCreateRequest request) {
        StageResponse stage = stageService.addStage(request);
        return ResponseEntity.ok(ApiResponseForm.success(stage, "단계 추가 성공"));
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ApiResponseForm<List<StageReadResponse>>> getStages(@PathVariable Long projectId) {
        List<StageReadResponse> stageReadResponseList = stageService.getStages(projectId);
        return ResponseEntity.ok(ApiResponseForm.success(stageReadResponseList, "단계 조회 성공"));
    }
}
