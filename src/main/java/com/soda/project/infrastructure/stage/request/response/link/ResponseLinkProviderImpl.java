package com.soda.project.infrastructure.stage.request.response.link;

import com.soda.project.domain.stage.request.response.link.ResponseLink;
import com.soda.project.domain.stage.request.response.link.ResponseLinkProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ResponseLinkProviderImpl implements ResponseLinkProvider {

    private final ResponseLinkRepository responseLinkRepository;

    @Override
    public void saveAll(List<ResponseLink> entities) {
        responseLinkRepository.saveAll(entities);
    }

    @Override
    public Optional<ResponseLink> findById(Long linkId) {
        return responseLinkRepository.findById(linkId);
    }
}
