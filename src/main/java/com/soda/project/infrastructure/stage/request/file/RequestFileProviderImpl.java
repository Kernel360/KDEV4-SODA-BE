package com.soda.project.infrastructure.stage.request.file;

import com.soda.project.domain.stage.request.file.RequestFile;
import com.soda.project.domain.stage.request.file.RequestFileProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RequestFileProviderImpl implements RequestFileProvider {

    private final RequestFileRepository requestFileRepository;

    @Override
    public void saveAll(List<RequestFile> entities) {
        requestFileRepository.saveAll(entities);
    }

    @Override
    public Optional<RequestFile> findById(Long fileId) {
        return requestFileRepository.findById(fileId);
    }
}
