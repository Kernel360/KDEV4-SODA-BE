package com.soda.request.controller;

import com.soda.global.response.ApiResponseForm;
import com.soda.global.security.auth.UserDetailsImpl;
import com.soda.request.dto.request.RequestDTO;
import com.soda.request.dto.request.RequestDeleteResponse;
import com.soda.request.dto.request.RequestUpdateRequest;
import com.soda.request.dto.request.RequestUpdateResponse;
import com.soda.request.dto.response.*;
import com.soda.request.service.ResponseService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/requests/{requestId}/responses")
    public ResponseEntity<ApiResponseForm<?>> getAllResponse(@PathVariable Long requestId) {
        List<ResponseDTO> responseDTOList = responseService.findAllByRequestId(requestId);
        return ResponseEntity.ok(ApiResponseForm.success(responseDTOList));
    }

    @GetMapping("responses/{responseId}")
    public ResponseEntity<ApiResponseForm<?>> getResponse(@PathVariable Long responseId) {
        ResponseDTO responseDTO = responseService.findById(responseId);
        return ResponseEntity.ok(ApiResponseForm.success(responseDTO));
    }

    @PutMapping("/responses/{responseId}")
    public ResponseEntity<ApiResponseForm<?>> updateResponse(@RequestBody ResponseUpdateRequest responseUpdateRequest,
                                                            @PathVariable Long responseId,
                                                            HttpServletRequest request) {
        Long memberId = (Long) request.getAttribute("memberId");
        ResponseUpdateResponse responseUpdateResponse = responseService.updateRequest(memberId, responseId, responseUpdateRequest);
        return ResponseEntity.ok(ApiResponseForm.success(responseUpdateResponse));
    }

    @DeleteMapping("/responses/{responseId}")
    public ResponseEntity<ApiResponseForm<?>> deleteRequest(@PathVariable Long responseId,
                                                            HttpServletRequest request) {
        Long memberId = (Long) request.getAttribute("memberId");
        ResponseDeleteResponse responseDeleteResponse = responseService.deleteResponse(memberId, responseId);
        return ResponseEntity.ok(ApiResponseForm.success(responseDeleteResponse));
    }
}
