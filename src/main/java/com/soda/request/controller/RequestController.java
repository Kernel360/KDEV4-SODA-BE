package com.soda.request.controller;

import com.soda.global.response.ApiResponseForm;
import com.soda.request.dto.file.FileUploadResponse;
import com.soda.request.dto.request.*;
import com.soda.request.service.RequestService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
                                                            HttpServletRequest request) {
        Long memberId = (Long) request.getAttribute("memberId");
        RequestDeleteResponse requestDeleteResponse = requestService.deleteRequest(memberId, requestId);
        return ResponseEntity.ok(ApiResponseForm.success(requestDeleteResponse));
    }

    @PostMapping("/requests/{requestId}/files")
    public ResponseEntity<ApiResponseForm<?>> uploadFiles(@PathVariable Long requestId,
                                                          @RequestPart("file") List<MultipartFile> files,
                                                          HttpServletRequest request) {
        Long memberId = (Long) request.getAttribute("memberId");
        FileUploadResponse fileUploadResponse = requestService.fileUpload(memberId, requestId, files);
        return ResponseEntity.ok(ApiResponseForm.success(fileUploadResponse));
    }
}