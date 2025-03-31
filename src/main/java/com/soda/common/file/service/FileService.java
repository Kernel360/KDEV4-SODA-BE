package com.soda.common.file.service;

import com.soda.common.file.dto.FileDeleteResponse;
import com.soda.common.file.dto.FileUploadResponse;
import com.soda.common.file.error.FileErrorCode;
import com.soda.common.file.model.FileBase;
import com.soda.common.file.strategy.FileStrategy;
import com.soda.global.response.GeneralException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    @Transactional
    public FileUploadResponse upload(String domainType, Long domainId, Long memberId, List<MultipartFile> files) {
        FileStrategy strategy = getStrategy(domainType);

        Object domain = strategy.getDomainOrThrow(domainId);
        strategy.validateWriter(memberId, domain);

        List<String> urls = s3Service.uploadFiles(files);

        List<FileBase> entities = IntStream.range(0, files.size())
                .mapToObj(i -> strategy.toEntity(files.get(i), urls.get(i), domain))
                .collect(Collectors.toList());

        strategy.saveAll(entities);
        return FileUploadResponse.fromEntity(entities);
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
