package com.soda.project.interfaces.stage.common.file.dto;

import com.soda.project.domain.stage.common.file.FileBase;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FileDTO {
    private Long id;
    private String name;
    private String url;

    public static <T extends FileBase> FileDTO fromEntity(T file) {
        return FileDTO.builder()
                .id(file.getId())
                .name(file.getName())
                .url(file.getUrl())
                .build();
    }
}
