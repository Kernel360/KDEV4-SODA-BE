package com.soda.request.service;

import com.soda.common.file.strategy.FileStrategy;
import com.soda.global.response.GeneralException;
import com.soda.request.entity.Request;
import com.soda.request.entity.RequestFile;
import com.soda.request.error.RequestErrorCode;
import com.soda.request.repository.RequestFileRepository;
import com.soda.request.repository.RequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class RequestFileStrategy implements FileStrategy<Request, RequestFile> {

    private final RequestRepository requestRepository;
    private final RequestFileRepository requestFileRepository;

    @Override
    public String getSupportedDomain() {
        return "request";
    }

    @Override
    public Request getDomainOrThrow(Long domainId) {
        return requestRepository.findById(domainId)
                .orElseThrow(() -> new GeneralException(RequestErrorCode.REQUEST_NOT_FOUND));
    }

    @Override
    public void validateWriter(Long memberId, Request request) {
        if (!request.getMember().getId().equals(memberId)) {
            throw new GeneralException(RequestErrorCode.USER_NOT_WRITE_REQUEST);
        }
    }

    @Override
    public RequestFile toEntity(MultipartFile file, String url, Request request) {
        return RequestFile.builder()
                .name(file.getOriginalFilename())
                .url(url)
                .request(request)
                .build();
    }

    @Override
    public void saveAll(List<RequestFile> entities) {
        requestFileRepository.saveAll(entities);
    }

    @Override
    public RequestFile getFileOrThrow(Long fileId) {
        return requestFileRepository.findById(fileId)
                .orElseThrow(() -> new GeneralException(RequestErrorCode.REQUESTFILE_NOT_FOUND));
    }

    @Override
    public void validateFileUploader(Long memberId, RequestFile file) {
        if (!file.getRequest().getMember().getId().equals(memberId)) {
            throw new GeneralException(RequestErrorCode.USER_NOT_UPLOAD_FILE);
        }
    }
}