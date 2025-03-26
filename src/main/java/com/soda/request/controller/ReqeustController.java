package com.soda.request.controller;

import com.soda.global.response.ApiResponseForm;
import com.soda.global.security.auth.UserDetailsImpl;
import com.soda.member.entity.Member;
import com.soda.request.dto.*;
import com.soda.request.service.RequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLOutput;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ReqeustController {
    private final RequestService requestService;

    @PostMapping("/requests")
    public ResponseEntity<ApiResponseForm<?>> createRequest(@RequestBody RequestCreateRequest requestCreateRequest,
                                                            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        RequestCreateResponse requestCreateResponse = requestService.createRequest(userDetails, requestCreateRequest);
        return ResponseEntity.ok(ApiResponseForm.success(requestCreateResponse));
    }

    @GetMapping("/tasks/{taskId}/requests")
    public ResponseEntity<ApiResponseForm<?>> getAllRequests(@PathVariable Long taskId) {
        List<RequestDTO> requestDTOList = requestService.findAllByTaskId(taskId);
        return ResponseEntity.ok(ApiResponseForm.success(requestDTOList));
    }

    @GetMapping("requests/{requestId}")
    public ResponseEntity<ApiResponseForm<?>> getRequest(@PathVariable Long requestId) {
        RequestDTO requestDTO = requestService.findById(requestId);
        return ResponseEntity.ok(ApiResponseForm.success(requestDTO));
    }

    @PutMapping("/requests/{requestId}")
    public ResponseEntity<ApiResponseForm<?>> updateRequest(@RequestBody RequestUpdateRequest requestUpdateRequest,
                                                            @PathVariable Long requestId,
                                                            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        RequestUpdateResponse requestUpdateResponse = requestService.updateRequest(userDetails, requestId, requestUpdateRequest);
        return ResponseEntity.ok(ApiResponseForm.success(requestUpdateResponse));
    }

    @DeleteMapping("/requests/{requestId}")
    public ResponseEntity<ApiResponseForm<?>> deleteRequest(@PathVariable Long requestId,
                                                            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        RequestDeleteResponse requestDeleteResponse = requestService.deleteRequest(userDetails, requestId);
        return ResponseEntity.ok(ApiResponseForm.success(requestDeleteResponse));
    }
}
