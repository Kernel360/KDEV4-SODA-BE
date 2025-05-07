package com.soda.project.domain.stage.request.response.link;

import java.util.List;
import java.util.Optional;

public interface ResponseLinkProvider {
    void saveAll(List<ResponseLink> entities);
    Optional<ResponseLink> findById(Long linkId);
}
