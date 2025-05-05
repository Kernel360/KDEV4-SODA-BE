package com.soda.project.interfaces;

import com.soda.common.file.dto.*;
import com.soda.common.file.service.FileService;
import com.soda.common.link.dto.LinkDeleteResponse;
import com.soda.common.link.dto.LinkUploadRequest;
import com.soda.common.link.dto.LinkUploadResponse;
import com.soda.common.link.service.LinkService;
import com.soda.global.response.ApiResponseForm;
import com.soda.project.application.stage.request.RequestFacade;
import com.soda.project.domain.stage.request.ApproverDesignationService;
import com.soda.project.domain.stage.request.RequestService;
import com.soda.project.domain.stage.request.dto.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RequestController {
    private final RequestFacade requestFacade;

    private final RequestService requestService;
    private final ApproverDesignationService approverDesignationService;
    private final FileService fileService;
    private final LinkService linkService;


    @PostMapping("/requests")
    public ResponseEntity<ApiResponseForm<?>> createRequest(@RequestBody RequestCreateRequest requestCreateRequest,
                                                            HttpServletRequest request) {
        Long memberId = (Long) request.getAttribute("memberId");
        RequestCreateResponse requestCreateResponse = requestFacade.createRequest(memberId, requestCreateRequest);
        return ResponseEntity.ok(ApiResponseForm.success(requestCreateResponse));
    }

    @PostMapping("/requests/{requestId}/re-requests")
    public ResponseEntity<ApiResponseForm<?>> createReRequest(@PathVariable Long requestId,
                                                              @RequestBody ReRequestCreateRequest reRequestCreateRequest,
                                                              HttpServletRequest request) {
        Long memberId = (Long) request.getAttribute("memberId");
        RequestCreateResponse requestCreateResponse = requestFacade.createReRequest(memberId, requestId, reRequestCreateRequest);
        return ResponseEntity.ok(ApiResponseForm.success(requestCreateResponse));
    }

    @GetMapping("/projects/{projectId}/requests")
    public ResponseEntity<ApiResponseForm<?>> getRequests(@PathVariable Long projectId,
                                                                 @ModelAttribute GetRequestCondition condition,
                                                                 Pageable pageable) {
        Page<RequestDTO> requests = requestFacade.findRequests(projectId, condition, pageable);
        return ResponseEntity.ok(ApiResponseForm.success(requests));
    }

    @GetMapping("/members/{memberId}/requests")
    public ResponseEntity<ApiResponseForm<?>> getMemberRequests(@PathVariable Long memberId,
                                                                @ModelAttribute GetMemberRequestCondition condition,
                                                                Pageable pageable) {
        Page<RequestDTO> requests = requestFacade.findMemberRequests(memberId, condition, pageable);
        return ResponseEntity.ok(ApiResponseForm.success(requests));
    }

    @GetMapping("/stages/{stageId}/requests")
    public ResponseEntity<ApiResponseForm<?>> getAllRequests(@PathVariable Long stageId) {
        List<RequestDTO> requestDTOList = requestFacade.findAllByStageId(stageId);
        return ResponseEntity.ok(ApiResponseForm.success(requestDTOList));
    }

    @GetMapping("requests/{requestId}")
    public ResponseEntity<ApiResponseForm<?>> getRequest(@PathVariable Long requestId) {
        RequestDTO requestDTO = requestFacade.findById(requestId);
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

    @PostMapping("requests/{requestId}/files/presigned-urls")
    public ResponseEntity<ApiResponseForm<?>> getPresingedUrl(@PathVariable Long requestId,
                                                         @RequestBody List<FileUploadRequest> fileUploadRequests,
                                                         HttpServletRequest request) {
        Long memberId = (Long) request.getAttribute("memberId");
        PresignedUploadResponse presignedUploadResponse = fileService.getPresignedUrls("request", requestId, memberId, fileUploadRequests);
        return ResponseEntity.ok(ApiResponseForm.success(presignedUploadResponse));
    }

    @PostMapping("requests/{requestId}/files/confirm-upload")
    public ResponseEntity<ApiResponseForm<?>> createFileMeta(@PathVariable Long requestId,
                                                         @RequestBody List<ConfirmedFile> confirmedFiles,
                                                         HttpServletRequest request) {
        Long memberId = (Long) request.getAttribute("memberId");
        FileConfirmResponse fileConfirmResponse = fileService.confirmUpload("request", requestId, memberId, confirmedFiles);
        return ResponseEntity.ok(ApiResponseForm.success(fileConfirmResponse));
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

    @DeleteMapping("requests/{requestId}/approver/{approverId}")
    public ResponseEntity<ApiResponseForm<?>> deleteApprover(@PathVariable Long approverId,
                                                             HttpServletRequest request) {
        Long memberId = (Long) request.getAttribute("memberId");
        ApproverDeleteResponse approverDeleteResponse = approverDesignationService.deleteApprover(approverId, memberId);
        return ResponseEntity.ok(ApiResponseForm.success(approverDeleteResponse));
    }
}