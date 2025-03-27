package com.soda.request.controller;

import com.soda.global.response.ApiResponseForm;
import com.soda.request.dto.response.RequestApproveRequest;
import com.soda.request.dto.response.RequestApproveResponse;
import com.soda.request.dto.response.RequestRejectRequest;
import com.soda.request.dto.response.RequestRejectResponse;
import com.soda.request.service.ResponseService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ResponseController {
    private final ResponseService responseService;

    @PostMapping("/requests/{requestId}/approval")
    public ResponseEntity<ApiResponseForm<?>> approveRequest(@RequestBody RequestApproveRequest requestApproveRequest,
                                                            @PathVariable Long requestId,
                                                            HttpServletRequest request) {
        Long memberId = (Long) request.getAttribute("memberId");
        RequestApproveResponse requestApproveResponse = responseService.approveRequest(memberId, requestId, requestApproveRequest);
        return ResponseEntity.ok(ApiResponseForm.success(requestApproveResponse));
    }

    @PostMapping("/requests/{requestId}/rejection")
    public ResponseEntity<ApiResponseForm<?>> rejectRequest(@RequestBody RequestRejectRequest requestRejectRequest,
                                                            @PathVariable Long requestId,
                                                            HttpServletRequest request) {
        Long memberId = (Long) request.getAttribute("memberId");
        RequestRejectResponse requestRejectResponse = responseService.rejectRequest(memberId, requestId, requestRejectRequest);
        return ResponseEntity.ok(ApiResponseForm.success(requestRejectResponse));
    }
}
