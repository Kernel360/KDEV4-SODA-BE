package com.soda.project.domain.stage.request.file;

import java.util.List;
import java.util.Optional;

public interface RequestFileProvider {
    void saveAll(List<RequestFile> entities);

    Optional<RequestFile> findById(Long fileId);
}
