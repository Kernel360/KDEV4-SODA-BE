package com.soda.project.domain.stage.request.response.dto;

import com.soda.common.link.dto.LinkUploadRequest;
import lombok.Getter;

import java.util.List;

@Getter
public class RequestApproveRequest {
    private String comment;
    private Long projectId;
    private List<LinkUploadRequest.LinkUploadDTO> links;
}
