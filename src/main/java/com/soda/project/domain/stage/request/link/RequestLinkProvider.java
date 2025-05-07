package com.soda.project.domain.stage.request.link;

import java.util.List;
import java.util.Optional;

public interface RequestLinkProvider {
    public void saveAll(List<RequestLink> entities);
    Optional<RequestLink> findById(Long linkId);
}
