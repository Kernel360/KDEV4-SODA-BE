package com.soda.project.domain.stage.common.file;

import com.soda.global.response.GeneralException;
import com.soda.project.interfaces.stage.common.file.dto.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class FileService {

    private final Map<String, FileStrategy> strategies;
    private final FileFactory fileFactory;

    public FileService(List<FileStrategy> strategies, FileFactory fileFactory) {
        this.strategies = strategies.stream()
                .collect(Collectors.toMap(FileStrategy::getSupportedDomain, Function.identity()));
        this.fileFactory = fileFactory;
    }

    public PresignedUploadResponse getPresignedUrls(String domainType, Long domainId, Long memberId, List<FileUploadRequest> files) {
        FileStrategy strategy = getStrategy(domainType);
        Object domain = strategy.getDomainOrThrow(domainId);
        strategy.validateWriter(memberId, domain);

        List<PresignedUploadResponse.Entry> entries = fileFactory.generateFileEntries(files);
        return PresignedUploadResponse.builder().entries(entries).build();
    }

    public FileConfirmResponse confirmUpload(String domainType, Long domainId, Long memberId, List<ConfirmedFile> confirmedFiles) {
        FileStrategy strategy = getStrategy(domainType);
        Object domain = strategy.getDomainOrThrow(domainId);
        strategy.validateWriter(memberId, domain);

        List<FileBase> entities = fileFactory.makeFileEntities(strategy, domain, confirmedFiles);
        strategy.saveAll(entities);
        return FileConfirmResponse.fromEntity(entities);
    }

    public FileDeleteResponse delete(String domainType, Long fileId, Long memberId) {
        FileStrategy strategy = getStrategy(domainType);
        FileBase file = strategy.getFileOrThrow(fileId);
        strategy.validateFileUploader(memberId, file);

        file.delete();
        return FileDeleteResponse.fromEntity(file);
    }

    private FileStrategy getStrategy(String domainType) {
        FileStrategy strategy = strategies.get(domainType.toLowerCase());
        if (strategy == null) { throw new GeneralException(FileErrorCode.FILE_DOMAIN_NOT_FOUND);}
        return strategy;
    }
}
