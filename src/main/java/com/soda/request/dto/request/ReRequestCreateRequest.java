package com.soda.request.dto.request;

import com.soda.common.link.dto.LinkUploadRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ReRequestCreateRequest {
    private String title;
    private String content;
    private List<LinkUploadRequest.LinkUploadDTO> links;
    private List<MemberAssignDTO> members;
}
