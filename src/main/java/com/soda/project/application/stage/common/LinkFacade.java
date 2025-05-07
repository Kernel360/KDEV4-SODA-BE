package com.soda.project.application.stage.common;

import com.soda.project.domain.stage.common.link.LinkService;
import com.soda.project.interfaces.stage.common.link.dto.LinkDeleteResponse;
import com.soda.project.interfaces.stage.common.link.dto.LinkUploadRequest;
import com.soda.project.interfaces.stage.common.link.dto.LinkUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LinkFacade {

    private final LinkService linkService;

    @Transactional
    public LinkUploadResponse upload(String domainType, Long domainId, Long memberId, LinkUploadRequest linkUploadRequest) {
        return linkService.upload(domainType, domainId, memberId, linkUploadRequest);
    }

    @Transactional
    public LinkDeleteResponse delete(String domainType, Long linkId, Long memberId) {
        return linkService.delete(domainType, linkId, memberId);
    }
}
