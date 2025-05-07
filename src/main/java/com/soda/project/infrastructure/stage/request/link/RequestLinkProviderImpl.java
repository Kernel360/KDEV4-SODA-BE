package com.soda.project.infrastructure.stage.request.link;

import com.soda.project.domain.stage.request.link.RequestLink;
import com.soda.project.domain.stage.request.link.RequestLinkProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RequestLinkProviderImpl implements RequestLinkProvider {

    private final RequestLinkRepository requestLinkRepository;

    @Override
    public void saveAll(List<RequestLink> entities) {
        requestLinkRepository.saveAll(entities);
    }

    @Override
    public Optional<RequestLink> findById(Long linkId) {
        return requestLinkRepository.findById(linkId);
    }
}
