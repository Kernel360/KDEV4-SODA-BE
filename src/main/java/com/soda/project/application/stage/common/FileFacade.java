package com.soda.project.application.stage.common;

import com.soda.project.domain.stage.common.file.FileService;
import com.soda.project.interfaces.stage.common.file.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FileFacade {

    private final FileService fileService;

    public PresignedUploadResponse getPresignedUrls(String domainType, Long domainId, Long memberId, List<FileUploadRequest> files) {
        return fileService.getPresignedUrls(domainType, domainId, memberId, files);
    }

    @Transactional
    public FileConfirmResponse confirmUpload(String domainType, Long domainId, Long memberId, List<ConfirmedFile> confirmedFiles) {
        return fileService.confirmUpload(domainType, domainId, memberId, confirmedFiles);
    }

    @Transactional
    public FileDeleteResponse delete(String domainType, Long fileId, Long memberId) {
        return fileService.delete(domainType, fileId, memberId);
    }
}