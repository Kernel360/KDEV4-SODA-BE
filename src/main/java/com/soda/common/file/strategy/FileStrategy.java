package com.soda.common.file.strategy;

import com.soda.common.file.model.FileBase;

import java.util.List;

public interface FileStrategy<T, E extends FileBase> {
    String getSupportedDomain(); // "request", "article" 등

    T getDomainOrThrow(Long domainId);

    void validateWriter(Long memberId, T domain);

    E toEntity(String fileName, String url, T domain);

    List<E> toEntities(List<String> url, List<String> names, T domain);

    void saveAll(List<E> entities);

    E getFileOrThrow(Long fileId);

    void validateFileUploader(Long memberId, E file);
}
