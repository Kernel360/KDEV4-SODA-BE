package com.soda.request.dto.file;

import com.soda.request.entity.RequestFile;
import lombok.Builder;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class FileUploadResponse {
    private Long requestId;
    private List<String> fileUrl;

    @Builder
    public static FileUploadResponse fromEntity(List<RequestFile> requestFiles) {
        Long requestId = requestFiles.get(0).getRequest().getId();
        List<String> fileUrls = requestFiles.stream()
                .map(RequestFile::getUrl)
                .collect(Collectors.toList());

        return FileUploadResponse.builder()
                .requestId(requestId)
                .fileUrl(fileUrls)
                .build();
    }
}
