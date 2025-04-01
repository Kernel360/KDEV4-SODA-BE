package com.soda.common.link.strategy;

import com.soda.common.link.dto.LinkUploadRequest;
import com.soda.common.link.model.LinkBase;

import java.util.List;

public interface LinkStrategy<T, E extends LinkBase> {
    String getSupportedDomain();

    T getDomainOrThrow(Long domainId);

    void validateWriter(Long memberId, T domain);

    E toEntity(LinkUploadRequest.LinkUploadDTO dto, T domain);

    void saveAll(List<E> entities);

    E getLinkOrThrow(Long linkId);

    void validateLinkUploader(Long memberId, E link);
}
