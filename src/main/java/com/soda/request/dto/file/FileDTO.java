package com.soda.request.dto.file;

import com.soda.request.entity.RequestFile;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FileDTO {
    private String name;
    private String url;

    public static FileDTO fromEntity(RequestFile requestFile) {
        return FileDTO.builder()
                .name(requestFile.getName())
                .url(requestFile.getUrl())
                .build();
    }
}
