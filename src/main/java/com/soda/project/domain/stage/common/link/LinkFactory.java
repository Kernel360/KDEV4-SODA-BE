package com.soda.project.domain.stage.common.link;

import com.soda.project.interfaces.stage.common.link.dto.LinkUploadRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class LinkFactory {
    public List<LinkBase> makeLinkEntities(LinkStrategy<Object, LinkBase> strategy, Object domain, LinkUploadRequest linkUploadRequest) {
        return linkUploadRequest.getLinks().stream()
                .map(dto -> strategy.toEntity(dto, domain))
                .collect(Collectors.toList());
    }
}
