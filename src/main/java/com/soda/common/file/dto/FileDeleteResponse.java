package com.soda.common.file.dto;

import com.soda.common.file.model.FileBase;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FileDeleteResponse {
    private Long id;
    private Long domainId;
    private String name;
    private String url;
    private Boolean isDeleted;

    public static <T extends FileBase> FileDeleteResponse fromEntity(T file) {
        return FileDeleteResponse.builder()
                .id(file.getId())
                .domainId(file.getDomainId())
                .name(file.getName())
                .url(file.getUrl())
                .isDeleted(file.getIsDeleted())
                .build();
    }
}
