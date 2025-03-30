package com.soda.common.file.dto;

import com.soda.common.file.model.FileBase;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class FileUploadResponse {
    private Long domainId;
    private List<String> fileUrl;

    public static <T extends FileBase> FileUploadResponse fromEntity(List<T> files) {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("파일 목록이 비어 있습니다.");
        }

        Long domainId = files.get(0).getDomainId();
        List<String> fileUrls = files.stream()
                .map(FileBase::getUrl)
                .collect(Collectors.toList());

        return FileUploadResponse.builder()
                .domainId(domainId)
                .fileUrl(fileUrls)
                .build();
    }
}
