package com.soda.common.link.service;

import com.soda.common.link.dto.LinkDeleteResponse;
import com.soda.common.link.dto.LinkUploadRequest;
import com.soda.common.link.dto.LinkUploadResponse;
import com.soda.common.link.error.LinkErrorCode;
import com.soda.common.link.model.LinkBase;
import com.soda.common.link.strategy.LinkStrategy;
import com.soda.global.response.GeneralException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class LinkService {

    private final Map<String, LinkStrategy> strategies;

    public LinkService(List<LinkStrategy> strategies) {
        this.strategies = strategies.stream()
                .collect(Collectors.toMap(LinkStrategy::getSupportedDomain, Function.identity()));
    }

    @Transactional
    public LinkUploadResponse upload(String domainType, Long domainId, Long memberId, LinkUploadRequest linkUploadRequest) {
        LinkStrategy strategy = getStrategy(domainType);

        Object domain = strategy.getDomainOrThrow(domainId);
        strategy.validateWriter(memberId, domain);

        List<LinkBase> entities = linkUploadRequest.getLinks().stream()
                .map(dto -> strategy.toEntity(dto, domain))
                .collect(Collectors.toList());

        strategy.saveAll(entities);
        return LinkUploadResponse.fromEntity(entities);
    }

    @Transactional
    public LinkDeleteResponse delete(String domainType, Long linkId, Long memberId) {
        LinkStrategy strategy = getStrategy(domainType);

        LinkBase link = strategy.getLinkOrThrow(linkId);
        strategy.validateLinkUploader(memberId, link);

        link.delete();

        return LinkDeleteResponse.fromEntity(link);
    }

    private LinkStrategy getStrategy(String domainType) {
        LinkStrategy strategy = strategies.get(domainType.toLowerCase());
        if (strategy == null) {
            throw new GeneralException(LinkErrorCode.LINK_DOMAIN_NOT_FOUND);
        }
        return strategy;
    }
}
