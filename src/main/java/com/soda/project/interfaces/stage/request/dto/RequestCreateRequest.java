package com.soda.project.interfaces.stage.request.dto;

import com.soda.project.interfaces.stage.common.link.dto.LinkUploadRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RequestCreateRequest {
    private String title;
    private String content;
    private Long projectId;
    private Long stageId;
    private List<LinkUploadRequest.LinkUploadDTO> links;
    private List<MemberAssignDTO> members;

}