package com.soda.request.dto.request;

import com.soda.common.link.dto.LinkUploadRequest;
import lombok.Getter;

import java.util.List;

@Getter
public class RequestUpdateRequest {
    private String title;
    private String content;
    private List<LinkUploadRequest.LinkUploadDTO> links;

    @Getter
    public static class LinkUploadDTO {
        private String urlAddress;
        private String urlDescription;
    }
}
