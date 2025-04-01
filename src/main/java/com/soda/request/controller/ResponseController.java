package com.soda.request.controller;

import com.soda.common.file.dto.FileDeleteResponse;
import com.soda.common.file.dto.FileUploadResponse;
import com.soda.common.file.service.FileService;
import com.soda.common.link.dto.LinkDeleteResponse;
import com.soda.common.link.service.LinkService;
import com.soda.global.response.ApiResponseForm;
import com.soda.request.dto.response.*;
import com.soda.request.service.ResponseService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ResponseController {
    private final ResponseService responseService;
    private final FileService fileService;
    private final LinkService linkService;

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

    @PostMapping("/responses/{responseId}/files")
    public ResponseEntity<ApiResponseForm<?>> uploadFiles(@PathVariable Long responseId,
                                                          @RequestPart("file") List<MultipartFile> files,
                                                          HttpServletRequest request) {
        Long memberId = (Long) request.getAttribute("memberId");
        FileUploadResponse fileUploadResponse = fileService.upload("response", responseId, memberId, files);
        return ResponseEntity.ok(ApiResponseForm.success(fileUploadResponse));
    }

    @DeleteMapping("responses/{responseId}/files/{fileId}")
    public ResponseEntity<ApiResponseForm<?>> deleteFile(@PathVariable Long fileId,
                                                         HttpServletRequest request) {
        Long memberId = (Long) request.getAttribute("memberId");
        FileDeleteResponse fileDeleteResponse = fileService.delete("response", memberId, fileId);
        return ResponseEntity.ok(ApiResponseForm.success(fileDeleteResponse));
    }

    @DeleteMapping("requests/{requestId}/links/{linkId}")
    public ResponseEntity<ApiResponseForm<?>> deleteLink(@PathVariable Long linkId,
                                                         HttpServletRequest request) {
        Long memberId = (Long) request.getAttribute("memberId");
        LinkDeleteResponse linkDeleteResponse = linkService.delete("response", memberId, linkId);
        return ResponseEntity.ok(ApiResponseForm.success(linkDeleteResponse));
    }
}
