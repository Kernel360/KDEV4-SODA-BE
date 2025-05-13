package com.soda.project.domain.stage.common.file;

import com.soda.project.interfaces.stage.common.file.dto.ConfirmedFile;
import com.soda.project.interfaces.stage.common.file.dto.FileUploadRequest;
import com.soda.project.interfaces.stage.common.file.dto.PresignedUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FileFactory {

    private final S3Service s3Service;

    public List<PresignedUploadResponse.Entry> generateFileEntries(List<FileUploadRequest> files) {
        return files.stream().map(file -> {
            PresignedUrlWithKey presigned = s3Service.generatePresignedPutUrl(file.getFileName(), file.getContentType());
            String fileUrl = s3Service.buildCloudFrontUrl(presigned.getKey());
            return new PresignedUploadResponse.Entry(presigned.getKey(), fileUrl, presigned.getUrl());
        }).collect(Collectors.toList());
    }


    public List<FileBase> makeFileEntities(FileStrategy<Object, FileBase> strategy, Object domain, List<ConfirmedFile> confirmedFiles) {
        return confirmedFiles.stream()
                .map(file -> strategy.toEntity(file.getFileName(), file.getUrl(), domain))
                .collect(Collectors.toList());
    }
}
