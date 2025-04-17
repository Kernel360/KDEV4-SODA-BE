package com.soda.request.dto.request;

import com.soda.common.link.dto.LinkUploadRequest;
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
    private List<MemberDTO> members;

    @Getter
    @Setter
    public static class MemberDTO {
        private Long id;
    }
}