package com.soda.project.domain.stage.request.file;

import com.soda.global.response.GeneralException;
import com.soda.project.domain.stage.common.file.FileStrategy;
import com.soda.project.domain.stage.request.Request;
import com.soda.project.domain.stage.request.RequestErrorCode;
import com.soda.project.domain.stage.request.RequestProvider;
import com.soda.project.infrastructure.stage.request.file.RequestFileRepository;
import com.soda.project.infrastructure.stage.request.RequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.IntStream;

@Service
@Transactional
@RequiredArgsConstructor
public class RequestFileStrategy implements FileStrategy<Request, RequestFile> {

    private final RequestProvider requestProvider;
    private final RequestFileProvider requestFileProvider;
    private final RequestRepository requestRepository;
    private final RequestFileRepository requestFileRepository;

    @Override
    public String getSupportedDomain() {
        return "request";
    }

    @Override
    public Request getDomainOrThrow(Long domainId) {
        return requestProvider.findById(domainId)
                .orElseThrow(() -> new GeneralException(RequestErrorCode.REQUEST_NOT_FOUND));
    }

    @Override
    public void validateWriter(Long memberId, Request request) {
        if (!request.getMember().getId().equals(memberId)) {
            throw new GeneralException(RequestErrorCode.USER_NOT_WRITE_REQUEST);
        }
    }

    @Override
    public RequestFile toEntity(String fileName, String url, Request request) {
        return RequestFile.create(fileName, url, request);
    }

    @Override
    public List<RequestFile> toEntities(List<String> urls, List<String> names, Request request) {
        if (urls == null || urls.isEmpty()) { return List.of();}

        return IntStream.range(0, urls.size())
                .mapToObj(i -> RequestFile.create(names.get(i), urls.get(i), request))
                .toList();
    }

    @Override
    public void saveAll(List<RequestFile> entities) {
        requestFileProvider.saveAll(entities);
    }

    @Override
    public RequestFile getFileOrThrow(Long fileId) {
        return requestFileProvider.findById(fileId)
                .orElseThrow(() -> new GeneralException(RequestErrorCode.REQUEST_FILE_NOT_FOUND));
    }

    @Override
    public void validateFileUploader(Long memberId, RequestFile file) {
        if (!file.getRequest().getMember().getId().equals(memberId)) {
            throw new GeneralException(RequestErrorCode.USER_NOT_UPLOAD_FILE);
        }
    }
}