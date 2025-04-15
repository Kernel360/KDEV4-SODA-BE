package com.soda.request.controller;

import com.soda.common.file.dto.FileDeleteResponse;
import com.soda.common.file.dto.FileUploadResponse;
import com.soda.common.file.service.FileService;
import com.soda.common.link.dto.LinkDeleteResponse;
import com.soda.common.link.dto.LinkUploadRequest;
import com.soda.common.link.dto.LinkUploadResponse;
import com.soda.common.link.service.LinkService;
import com.soda.global.response.ApiResponseForm;
import com.soda.request.dto.GetRequestCondition;
import com.soda.request.dto.request.*;
import com.soda.request.service.RequestService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RequestController {
    private final RequestService requestService;
    private final FileService fileService;
    private final LinkService linkService;


    @PostMapping("/requests")
    public ResponseEntity<ApiResponseForm<?>> createRequest(@RequestBody RequestCreateRequest requestCreateRequest,
                                                            HttpServletRequest request) {
        Long memberId = (Long) request.getAttribute("memberId");
        RequestCreateResponse requestCreateResponse = requestService.createRequest(memberId, requestCreateRequest);
        return ResponseEntity.ok(ApiResponseForm.success(requestCreateResponse));
    }

    @GetMapping("/projects/{projectId}/requests")
    public ResponseEntity<ApiResponseForm<?>> getRequests(@PathVariable Long projectId,
                                                                 @ModelAttribute GetRequestCondition condition,
                                                                 Pageable pageable) {
        Page<RequestDTO> requests = requestService.findRequests(condition, pageable);
        return ResponseEntity.ok(ApiResponseForm.success(requests));
    }

    @GetMapping("/stages/{stageId}/requests")
    public ResponseEntity<ApiResponseForm<?>> getAllRequests(@PathVariable Long stageId) {
        List<RequestDTO> requestDTOList = requestService.findAllByStageId(stageId);
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

    @PostMapping(path = "/requests/{requestId}/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponseForm<?>> uploadFiles(@PathVariable Long requestId,
                                                          @RequestPart("file") List<MultipartFile> files,
                                                          HttpServletRequest request) {
        Long memberId = (Long) request.getAttribute("memberId");
        FileUploadResponse fileUploadResponse = fileService.upload("request", requestId, memberId, files);
        return ResponseEntity.ok(ApiResponseForm.success(fileUploadResponse));
    }

    @DeleteMapping("requests/{requestId}/files/{fileId}")
    public ResponseEntity<ApiResponseForm<?>> deleteFile(@PathVariable Long fileId,
                                                         HttpServletRequest request) {
        Long memberId = (Long) request.getAttribute("memberId");
        FileDeleteResponse fileDeleteResponse = fileService.delete("request", fileId, memberId);
        return ResponseEntity.ok(ApiResponseForm.success(fileDeleteResponse));
    }

    @PostMapping("/requests/{requestId}/links")
    public ResponseEntity<ApiResponseForm<?>> uploadLinks(@PathVariable Long requestId,
                                                          @RequestBody LinkUploadRequest requestLinkUploadRequest,
                                                          HttpServletRequest request) {
        Long memberId = (Long) request.getAttribute("memberId");
        LinkUploadResponse linkUploadResponse = linkService.upload("request", requestId, memberId, requestLinkUploadRequest);
        return ResponseEntity.ok(ApiResponseForm.success(linkUploadResponse));
    }

    @DeleteMapping("requests/{requestId}/links/{linkId}")
    public ResponseEntity<ApiResponseForm<?>> deleteLink(@PathVariable Long linkId,
                                                         HttpServletRequest request) {
        Long memberId = (Long) request.getAttribute("memberId");
        LinkDeleteResponse linkDeleteResponse = linkService.delete("request", linkId, memberId);
        return ResponseEntity.ok(ApiResponseForm.success(linkDeleteResponse));
    }
}