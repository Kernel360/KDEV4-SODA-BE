package com.soda.project.infrastructure.stage.request.response.file;

import com.soda.project.domain.stage.request.response.file.ResponseFile;
import com.soda.project.domain.stage.request.response.file.ResponseFileProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ResponseFileProviderImpl implements ResponseFileProvider {

    private final ResponseFileRepository responseFileRepository;

    @Override
    public void saveAll(List<ResponseFile> entities) {
        responseFileRepository.saveAll(entities);
    }

    @Override
    public Optional<ResponseFile> findById(Long fileId) {
        return responseFileRepository.findById(fileId);
    }
}
