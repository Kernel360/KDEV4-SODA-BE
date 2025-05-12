package com.soda.project.interfaces.stage.request.response;

import com.soda.global.response.ApiResponseForm;
import com.soda.project.application.stage.common.FileFacade;
import com.soda.project.application.stage.common.LinkFacade;
import com.soda.project.application.stage.request.response.ResponseFacade;
import com.soda.project.interfaces.stage.common.file.dto.*;
import com.soda.project.interfaces.stage.common.link.dto.LinkDeleteResponse;
import com.soda.project.interfaces.stage.request.response.dto.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ResponseController {
    private final ResponseFacade responseFacade;
    private final FileFacade fileFacade;
    private final LinkFacade linkFacade;

    @PostMapping("/requests/{requestId}/approval")
    public ResponseEntity<ApiResponseForm<?>> approveRequest(@RequestBody RequestApproveRequest requestApproveRequest,
                                                            @PathVariable Long requestId,
                                                            HttpServletRequest request) {
        Long memberId = (Long) request.getAttribute("memberId");
        RequestApproveResponse requestApproveResponse = responseFacade.approveRequest(memberId, requestId, requestApproveRequest);
        return ResponseEntity.ok(ApiResponseForm.success(requestApproveResponse));
    }

    @PostMapping("/requests/{requestId}/rejection")
    public ResponseEntity<ApiResponseForm<?>> rejectRequest(@RequestBody RequestRejectRequest requestRejectRequest,
                                                            @PathVariable Long requestId,
                                                            HttpServletRequest request) {
        Long memberId = (Long) request.getAttribute("memberId");
        RequestRejectResponse requestRejectResponse = responseFacade.rejectRequest(memberId, requestId, requestRejectRequest);
        return ResponseEntity.ok(ApiResponseForm.success(requestRejectResponse));
    }

    @GetMapping("/requests/{requestId}/responses")
    public ResponseEntity<ApiResponseForm<?>> getAllResponse(@PathVariable Long requestId) {
        List<ResponseDTO> responseDTOList = responseFacade.findAllByRequestId(requestId);
        return ResponseEntity.ok(ApiResponseForm.success(responseDTOList));
    }

    @GetMapping("responses/{responseId}")
    public ResponseEntity<ApiResponseForm<?>> getResponse(@PathVariable Long responseId) {
        ResponseDTO responseDTO = responseFacade.findById(responseId);
        return ResponseEntity.ok(ApiResponseForm.success(responseDTO));
    }

    @PutMapping("/responses/{responseId}")
    public ResponseEntity<ApiResponseForm<?>> updateResponse(@RequestBody ResponseUpdateRequest responseUpdateRequest,
                                                            @PathVariable Long responseId,
                                                            HttpServletRequest request) {
        Long memberId = (Long) request.getAttribute("memberId");
        ResponseUpdateResponse responseUpdateResponse = responseFacade.updateResponse(memberId, responseId, responseUpdateRequest);
        return ResponseEntity.ok(ApiResponseForm.success(responseUpdateResponse));
    }

    @DeleteMapping("/responses/{responseId}")
    public ResponseEntity<ApiResponseForm<?>> deleteRequest(@PathVariable Long responseId,
                                                            HttpServletRequest request) {
        Long memberId = (Long) request.getAttribute("memberId");
        ResponseDeleteResponse responseDeleteResponse = responseFacade.deleteResponse(memberId, responseId);
        return ResponseEntity.ok(ApiResponseForm.success(responseDeleteResponse));
    }

    @PostMapping("responses/{responseId}/files/presigned-urls")
    public ResponseEntity<ApiResponseForm<?>> getPresingedUrl(@PathVariable Long responseId,
                                                              @RequestBody List<FileUploadRequest> fileUploadRequests,
                                                              HttpServletRequest request) {
        Long memberId = (Long) request.getAttribute("memberId");
        PresignedUploadResponse presignedUploadResponse = fileFacade.getPresignedUrls("response", responseId, memberId, fileUploadRequests);
        return ResponseEntity.ok(ApiResponseForm.success(presignedUploadResponse));
    }

    @PostMapping("responses/{responseId}/files/confirm-upload")
    public ResponseEntity<ApiResponseForm<?>> createFileMeta(@PathVariable Long responseId,
                                                             @RequestBody List<ConfirmedFile> confirmedFiles,
                                                             HttpServletRequest request) {
        Long memberId = (Long) request.getAttribute("memberId");
        FileConfirmResponse fileConfirmResponse = fileFacade.confirmUpload("response", responseId, memberId, confirmedFiles);
        return ResponseEntity.ok(ApiResponseForm.success(fileConfirmResponse));
    }

    @DeleteMapping("responses/{responseId}/files/{fileId}")
    public ResponseEntity<ApiResponseForm<?>> deleteFile(@PathVariable Long fileId,
                                                         HttpServletRequest request) {
        Long memberId = (Long) request.getAttribute("memberId");
        FileDeleteResponse fileDeleteResponse = fileFacade.delete("response", fileId, memberId);
        return ResponseEntity.ok(ApiResponseForm.success(fileDeleteResponse));
    }

    @DeleteMapping("responses/{responseId}/links/{linkId}")
    public ResponseEntity<ApiResponseForm<?>> deleteLink(@PathVariable Long linkId,
                                                         HttpServletRequest request) {
        Long memberId = (Long) request.getAttribute("memberId");
        LinkDeleteResponse linkDeleteResponse = linkFacade.delete("response", linkId, memberId);
        return ResponseEntity.ok(ApiResponseForm.success(linkDeleteResponse));
    }
}
