package com.soda.project.application.stage.common;

import com.soda.global.response.GeneralException;
import com.soda.project.domain.stage.common.file.FileBase;
import com.soda.project.domain.stage.common.file.FileErrorCode;
import com.soda.project.domain.stage.common.file.FileService;
import com.soda.project.domain.stage.common.file.FileStrategy;
import com.soda.project.interfaces.stage.common.file.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class FileFacade {
    private final Map<String, FileStrategy<Object, FileBase>> strategies;
    private final FileService fileService;

    public FileFacade(List<FileStrategy<Object, FileBase>> strategies, FileService fileService) {
        this.strategies = strategies.stream()
                .collect(Collectors.toMap(FileStrategy::getSupportedDomain, Function.identity()));
        this.fileService = fileService;
    }

    public PresignedUploadResponse getPresignedUrls(String domainType, Long domainId, Long memberId, List<FileUploadRequest> files) {
        FileStrategy<Object, FileBase> strategy = getStrategy(domainType);
        Object domain = strategy.getDomainOrThrow(domainId);
        strategy.validateWriter(memberId, domain);

        return fileService.getPresignedUrls(files);
    }

    @Transactional
    public FileConfirmResponse confirmUpload(String domainType, Long domainId, Long memberId, List<ConfirmedFile> confirmedFiles) {
        FileStrategy<Object, FileBase> strategy = getStrategy(domainType);
        Object domain = strategy.getDomainOrThrow(domainId);
        strategy.validateWriter(memberId, domain);

        return fileService.confirmUpload(strategy, domain, confirmedFiles);
    }

    @Transactional
    public FileDeleteResponse delete(String domainType, Long fileId, Long memberId) {
        FileStrategy<Object, FileBase> strategy = getStrategy(domainType);
        FileBase file = strategy.getFileOrThrow(fileId);
        strategy.validateFileUploader(memberId, file);

        return fileService.delete(file);
    }

    private FileStrategy<Object, FileBase> getStrategy(String domainType) {
        FileStrategy<Object, FileBase> strategy = strategies.get(domainType.toLowerCase());
        if (strategy == null) { throw new GeneralException(FileErrorCode.FILE_DOMAIN_NOT_FOUND);}
        return strategy;
    }
}