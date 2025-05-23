package com.soda.project.interfaces.stage.request.dto;

import com.soda.project.interfaces.stage.common.link.dto.LinkUploadRequest;
import lombok.Getter;

import java.util.List;

@Getter
public class RequestUpdateRequest {
    private String title;
    private String content;
    private List<LinkUploadRequest.LinkUploadDTO> links;
    private List<MemberAssignDTO> members;

    @Getter
    public static class LinkUploadDTO {
        private String urlAddress;
        private String urlDescription;
    }
}
