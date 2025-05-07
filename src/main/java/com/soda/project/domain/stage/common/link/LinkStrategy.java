package com.soda.project.domain.stage.common.link;

import com.soda.project.interfaces.stage.common.link.dto.LinkUploadRequest;

import java.util.List;

public interface LinkStrategy<T, E extends LinkBase> {
    String getSupportedDomain();

    T getDomainOrThrow(Long domainId);

    void validateWriter(Long memberId, T domain);

    E toEntity(LinkUploadRequest.LinkUploadDTO dto, T domain);

    List<E> toEntities(List<? extends LinkUploadRequest.LinkUploadDTO> dtos, Object domain);

    void saveAll(List<E> entities);

    E getLinkOrThrow(Long linkId);

    void validateLinkUploader(Long memberId, E link);
}
