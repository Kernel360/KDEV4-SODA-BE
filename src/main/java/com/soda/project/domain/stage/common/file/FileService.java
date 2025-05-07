package com.soda.project.domain.stage.common.file;

import com.soda.global.response.GeneralException;
import com.soda.project.interfaces.stage.common.file.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class FileService {

    private final Map<String, FileStrategy> strategies;
    private final S3Service s3Service;

    public FileService(List<FileStrategy> strategies, S3Service s3Service) {
        this.strategies = strategies.stream()
                .collect(Collectors.toMap(FileStrategy::getSupportedDomain, Function.identity()));
        this.s3Service = s3Service;
    }

    public PresignedUploadResponse getPresignedUrls(String domainType, Long domainId, Long memberId, List<FileUploadRequest> files) {
        FileStrategy strategy = getStrategy(domainType);
        Object domain = strategy.getDomainOrThrow(domainId);
        strategy.validateWriter(memberId, domain);

        List<PresignedUploadResponse.Entry> entries = files.stream().map(file -> {
            String presignedUrl = s3Service.generatePresignedPutUrl(file.getFileName(), file.getContentType());
            String fileUrl = s3Service.buildCloudFrontUrl(file.getFileName());
            return new PresignedUploadResponse.Entry(file.getFileName(), fileUrl, presignedUrl);
        }).collect(Collectors.toList());

        return PresignedUploadResponse.builder().entries(entries).build();
    }

    @Transactional
    public FileConfirmResponse confirmUpload(String domainType, Long domainId, Long memberId, List<ConfirmedFile> confirmedFiles) {
        FileStrategy strategy = getStrategy(domainType);
        Object domain = strategy.getDomainOrThrow(domainId);
        strategy.validateWriter(memberId, domain);

        List<FileBase> entities = confirmedFiles.stream()
                .map(file -> strategy.toEntity(file.getFileName(), file.getUrl(), domain))
                .collect(Collectors.toList());

        strategy.saveAll(entities);
        return FileConfirmResponse.fromEntity(entities);
    }

    @Transactional
    public FileDeleteResponse delete(String domainType, Long fileId, Long memberId) {
        FileStrategy strategy = getStrategy(domainType);
        FileBase file = strategy.getFileOrThrow(fileId);
        strategy.validateFileUploader(memberId, file);

        file.delete();

        return FileDeleteResponse.fromEntity(file);
    }

    private FileStrategy getStrategy(String domainType) {
        FileStrategy strategy = strategies.get(domainType.toLowerCase());
        if (strategy == null) {
            throw new GeneralException(FileErrorCode.FILE_DOMAIN_NOT_FOUND);
        }
        return strategy;
    }
}
