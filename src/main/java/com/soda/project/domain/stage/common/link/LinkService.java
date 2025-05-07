package com.soda.project.domain.stage.common.link;

import com.soda.global.response.GeneralException;
import com.soda.project.interfaces.stage.common.link.dto.LinkDeleteResponse;
import com.soda.project.interfaces.stage.common.link.dto.LinkUploadRequest;
import com.soda.project.interfaces.stage.common.link.dto.LinkUploadResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class LinkService {

    private final Map<String, LinkStrategy> strategies;
    private final LinkFactory linkFactory;

    public LinkService(List<LinkStrategy> strategies, LinkFactory linkFactory) {
        this.strategies = strategies.stream()
                .collect(Collectors.toMap(LinkStrategy::getSupportedDomain, Function.identity()));
        this.linkFactory = linkFactory;
    }

    public LinkUploadResponse upload(String domainType, Long domainId, Long memberId, LinkUploadRequest linkUploadRequest) {
        LinkStrategy strategy = getStrategy(domainType);
        Object domain = strategy.getDomainOrThrow(domainId);
        strategy.validateWriter(memberId, domain);

        List<LinkBase> entities = linkFactory.makeLinkEntities(strategy, domain, linkUploadRequest);
        strategy.saveAll(entities);
        return LinkUploadResponse.fromEntity(entities);
    }

    public LinkDeleteResponse delete(String domainType, Long linkId, Long memberId) {
        LinkStrategy strategy = getStrategy(domainType);
        LinkBase link = strategy.getLinkOrThrow(linkId);
        strategy.validateLinkUploader(memberId, link);
        link.delete();
        return LinkDeleteResponse.fromEntity(link);
    }

    @Transactional
    public <T extends LinkBase> List<T> buildLinks(String domainType, Object domain, List<? extends LinkUploadRequest.LinkUploadDTO> dtos) {
        LinkStrategy strategy = getStrategy(domainType);
        return (List<T>) strategy.toEntities(dtos, domain);
    }

    private LinkStrategy getStrategy(String domainType) {
        LinkStrategy strategy = strategies.get(domainType.toLowerCase());
        if (strategy == null) { throw new GeneralException(LinkErrorCode.LINK_DOMAIN_NOT_FOUND);}
        return strategy;
    }
}
