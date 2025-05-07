package com.soda.project.interfaces.stage.common.file.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class PresignedUploadResponse {
    private List<Entry> entries;

    @Getter
    @AllArgsConstructor
    public static class Entry {
        private String fileName;
        private String fileUrl;
        private String presignedUrl;
    }
}
