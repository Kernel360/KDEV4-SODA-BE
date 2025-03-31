package com.soda.common.file.dto;

import com.soda.common.file.error.FileErrorCode;
import com.soda.common.file.model.FileBase;
import com.soda.global.response.GeneralException;
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
            throw new GeneralException(FileErrorCode.FILE_LIST_EMPTY);
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
