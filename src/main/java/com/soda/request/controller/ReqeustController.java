package com.soda.request.controller;

import com.soda.global.response.ApiResponseForm;
import com.soda.request.dto.*;
import com.soda.request.service.RequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
public class ReqeustController {
    private final RequestService requestService;

    @PostMapping("/projects/{projectId}/stages/{stageId}/tasks/{taskId}/requests")
    public ResponseEntity<ApiResponseForm<?>> createRequest(@RequestBody RequestCreateRequest requestCreateRequest, @PathVariable Long projectId, @PathVariable Long stageId, @PathVariable Long taskId) {
        Long memberId = (long)(Math.random() * 987654321) + 1; // 회원기능 추가되면 삭제해야함. 임시로 memberId 난수 생성하도록 만들어두었음.
        RequestCreateResponse requestCreateResponse = requestService.createRequest(memberId, projectId, requestCreateRequest);
        return ResponseEntity.ok(ApiResponseForm.success(requestCreateResponse));
    }

    @GetMapping("tasks/{taskId}/requests")
    public ResponseEntity<ApiResponseForm<?>> getAllRequests(@PathVariable Long taskId) {
        List<RequestDTO> requestDTOList = requestService.findAllByTaskId(taskId);
        return ResponseEntity.ok(ApiResponseForm.success(requestDTOList));
    }

    @PutMapping("/projects/{projectId}/stages/{stageId}/tasks/{taskId}/requests/{requestId}")
    public ResponseEntity<ApiResponseForm<?>> updateRequest(@RequestBody RequestUpdateRequest requestUpdateRequest, @PathVariable Long projectId, @PathVariable Long stageId, @PathVariable Long taskId) {
        Long memberId = (long)(Math.random() * 987654321) + 1; // 회원기능 추가되면 삭제해야함. 임시로 memberId 난수 생성하도록 만들어두었음.
        RequestUpdateResponse requestUpdateResponse = requestService.updateRequest(memberId, requestUpdateRequest);
        return ResponseEntity.ok(ApiResponseForm.success(requestUpdateResponse));
    }

    @DeleteMapping("/projects/{projectId}/stages/{stageId}/tasks/{taskId}/requests/{requestId}")
    public ResponseEntity<ApiResponseForm<?>> deleteRequest(@PathVariable Long projectId, @PathVariable Long stageId, @PathVariable Long taskId, @PathVariable Long requestId) {
        Long memberId = (long)(Math.random() * 987654321) + 1; // 회원기능 추가되면 삭제해야함. 임시로 memberId 난수 생성하도록 만들어두었음.
        RequestDeleteResponse requestDeleteResponse = requestService.deleteRequest(memberId, requestId);
        return ResponseEntity.ok(ApiResponseForm.success(requestDeleteResponse));
    }
}
