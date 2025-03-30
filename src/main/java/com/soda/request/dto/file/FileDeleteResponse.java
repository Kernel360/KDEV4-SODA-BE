package com.soda.request.dto.file;

import com.soda.request.entity.RequestFile;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FileDeleteResponse {
    private Long id;
    private Long requestId;
    private String name;
    private String url;

    public static FileDeleteResponse fromEntity(RequestFile requestFile) {
        return FileDeleteResponse.builder()
                .id(requestFile.getId())
                .requestId(requestFile.getRequest().getId())
                .name(requestFile.getName())
                .url(requestFile.getUrl())
                .build();
    }
}
