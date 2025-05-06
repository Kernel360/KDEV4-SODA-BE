package com.soda.project.interfaces.stage;

import com.soda.global.response.ApiResponseForm;
import com.soda.project.domain.stage.StageService;
import com.soda.project.interfaces.stage.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class StageController {

    private final StageService stageService;

    @PostMapping("/stages")
    public ResponseEntity<ApiResponseForm<StageResponse>> addStage(@RequestBody StageCreateRequest request) {
        StageResponse stage = stageService.addStage(request);
        return ResponseEntity.ok(ApiResponseForm.success(stage, "단계 추가 성공"));
    }

    @GetMapping("/projects/{projectId}/stages")
    public ResponseEntity<ApiResponseForm<List<StageReadResponse>>> getStages(@PathVariable Long projectId) {
        List<StageReadResponse> stageReadResponseList = stageService.getStages(projectId);
        return ResponseEntity.ok(ApiResponseForm.success(stageReadResponseList, "단계 조회 성공"));
    }

    @PutMapping("/stages/{stageId}/move")
    public ResponseEntity<ApiResponseForm<Void>> moveStage(@PathVariable Long stageId,
                                                           @RequestBody StageMoveRequest request) {
        stageService.moveStage(stageId, request);
        return ResponseEntity.ok(ApiResponseForm.success(null, "단계 이동 성공"));
    }

    @DeleteMapping("/stages/{stageId}")
    public ResponseEntity<ApiResponseForm<Void>> deleteStage(@PathVariable Long stageId) {
        stageService.deleteStage(stageId);
        return ResponseEntity.ok(ApiResponseForm.success(null, "단계 삭제 성공"));
    }

    @PutMapping("/stages/{stageId}")
    public ResponseEntity<ApiResponseForm<Void>> updateStage(@PathVariable Long stageId, @RequestBody StageUpdateRequest request) {
        stageService.updateStage(stageId, request);
        return ResponseEntity.ok(ApiResponseForm.success(null, "단계 수정 성공"));
    }
}
