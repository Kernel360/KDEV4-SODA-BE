package com.soda.request.strategy;

import com.soda.common.file.strategy.FileStrategy;
import com.soda.global.response.GeneralException;
import com.soda.request.entity.Response;
import com.soda.request.entity.ResponseFile;
import com.soda.request.error.ResponseErrorCode;
import com.soda.request.repository.ResponseFileRepository;
import com.soda.request.repository.ResponseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.IntStream;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ResponseFileStrategy implements FileStrategy<Response, ResponseFile> {

    private final ResponseRepository responseRepository;
    private final ResponseFileRepository responseFileRepository;

    @Override
    public String getSupportedDomain() {
        return "response";
    }

    @Override
    public Response getDomainOrThrow(Long domainId) {
        return responseRepository.findById(domainId)
                .orElseThrow(() -> new GeneralException(ResponseErrorCode.RESPONSE_NOT_FOUND));
    }

    @Override
    public void validateWriter(Long memberId, Response Response) {
        if (!Response.getMember().getId().equals(memberId)) {
            throw new GeneralException(ResponseErrorCode.USER_NOT_WRITE_RESPONSE);
        }
    }

    @Override
    public ResponseFile toEntity(String fileName, String url, Response Response) {
        return ResponseFile.builder()
                .name(fileName)
                .url(url)
                .response(Response)
                .build();
    }

    @Override
    public List<ResponseFile> toEntities(List<String> urls, List<String> names, Response domain) {
        if (urls == null || urls.isEmpty()) {
            return List.of();
        }

        return IntStream.range(0, urls.size())
                .mapToObj(i -> ResponseFile.builder()
                        .url(urls.get(i))
                        .name(names.get(i))
                        .response(domain)
                        .build())
                .toList();
    }

    @Override
    public void saveAll(List<ResponseFile> entities) {
        responseFileRepository.saveAll(entities);
    }

    @Override
    public ResponseFile getFileOrThrow(Long fileId) {
        return responseFileRepository.findById(fileId)
                .orElseThrow(() -> new GeneralException(ResponseErrorCode.RESPONSE_FILE_NOT_FOUND));
    }

    @Override
    public void validateFileUploader(Long memberId, ResponseFile file) {
        if (!file.getResponse().getMember().getId().equals(memberId)) {
            throw new GeneralException(ResponseErrorCode.USER_NOT_UPLOAD_FILE);
        }
    }
}
