package com.soda.project.domain.stage.request.response.file;

import java.util.List;
import java.util.Optional;

public interface ResponseFileProvider {
    void saveAll(List<ResponseFile> entities);
    Optional<ResponseFile> findById(Long fileId);
}
