package com.soda.request.dto.response;

import com.soda.common.link.dto.LinkUploadRequest;
import lombok.Getter;

import java.util.List;

@Getter
public class ResponseUpdateRequest {
    private String comment;
    private Long projectId;
    private List<LinkUploadRequest.LinkUploadDTO> links;
}