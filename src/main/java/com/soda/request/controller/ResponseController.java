package com.soda.request.controller;

import com.soda.global.response.ApiResponseForm;
import com.soda.global.security.auth.UserDetailsImpl;
import com.soda.request.dto.RequestApproveRequest;
import com.soda.request.dto.RequestApproveResponse;
import com.soda.request.dto.RequestUpdateRequest;
import com.soda.request.dto.RequestUpdateResponse;
import com.soda.request.service.ResponseService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ResponseController {
    private final ResponseService responseService;

    @PatchMapping("/requests/{requestId}/status")
    public ResponseEntity<ApiResponseForm<?>> updateRequest(@RequestBody RequestApproveRequest requestApproveRequest,
                                                            @PathVariable Long requestId,
                                                            HttpServletRequest request) {
        Long memberId = (Long) request.getAttribute("memberId");
        RequestApproveResponse requestApproveResponse = responseService.approveRequest(memberId, requestId, requestApproveRequest);
        return ResponseEntity.ok(ApiResponseForm.success(requestApproveResponse));
    }
}
