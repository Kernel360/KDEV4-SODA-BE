package com.soda.request.controller;

import com.soda.global.response.ApiResponseForm;
import com.soda.global.security.auth.UserDetailsImpl;
import com.soda.request.dto.request.*;
import com.soda.request.service.RequestService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RequestController {
    private final RequestService requestService;

    @PostMapping("/requests")
    public ResponseEntity<ApiResponseForm<?>> createRequest(@RequestBody RequestCreateRequest requestCreateRequest,
                                                            HttpServletRequest request) {
        Long memberId = (Long) request.getAttribute("memberId");
        RequestCreateResponse requestCreateResponse = requestService.createRequest(memberId, requestCreateRequest);
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
                                                            HttpServletRequest request) {
        Long memberId = (Long) request.getAttribute("memberId");
        RequestUpdateResponse requestUpdateResponse = requestService.updateRequest(memberId, requestId, requestUpdateRequest);
        return ResponseEntity.ok(ApiResponseForm.success(requestUpdateResponse));
    }

    @DeleteMapping("/requests/{requestId}")
    public ResponseEntity<ApiResponseForm<?>> deleteRequest(@PathVariable Long requestId,
                                                            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        RequestDeleteResponse requestDeleteResponse = requestService.deleteRequest(userDetails, requestId);
        return ResponseEntity.ok(ApiResponseForm.success(requestDeleteResponse));
    }
}
