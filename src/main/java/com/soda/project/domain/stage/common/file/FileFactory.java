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
            String presignedUrl = s3Service.generatePresignedPutUrl(file.getFileName(), file.getContentType());
            String fileUrl = s3Service.buildCloudFrontUrl(file.getFileName());
            return new PresignedUploadResponse.Entry(file.getFileName(), fileUrl, presignedUrl);
        }).collect(Collectors.toList());
    }

    public List<FileBase> makeFileEntities(FileStrategy strategy, Object domain, List<ConfirmedFile> confirmedFiles) {
        return confirmedFiles.stream()
                .map(file -> strategy.toEntity(file.getFileName(), file.getUrl(), domain))
                .collect(Collectors.toList());
    }
}
