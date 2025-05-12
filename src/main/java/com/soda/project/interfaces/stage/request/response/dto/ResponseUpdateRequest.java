package com.soda.project.interfaces.stage.request.response.dto;

import com.soda.project.interfaces.stage.common.link.dto.LinkUploadRequest;
import lombok.Getter;

import java.util.List;

@Getter
public class ResponseUpdateRequest {
    private String comment;
    private Long projectId;
    private List<LinkUploadRequest.LinkUploadDTO> links;
}