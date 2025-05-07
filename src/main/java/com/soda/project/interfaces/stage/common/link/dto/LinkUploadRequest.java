package com.soda.project.interfaces.stage.common.link.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class LinkUploadRequest {
    private List<LinkUploadDTO> links;

    @Getter
    public static class LinkUploadDTO {
        private String urlAddress;
        private String urlDescription;
    }
}
