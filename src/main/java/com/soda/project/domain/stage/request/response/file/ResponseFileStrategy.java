package com.soda.project.domain.stage.request.response.file;

import com.soda.global.response.GeneralException;
import com.soda.project.domain.stage.common.file.FileStrategy;
import com.soda.project.domain.stage.request.response.Response;
import com.soda.project.domain.stage.request.response.ResponseErrorCode;
import com.soda.project.domain.stage.request.response.ResponseProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.IntStream;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ResponseFileStrategy implements FileStrategy<Response, ResponseFile> {

    private final ResponseProvider responseProvider;
    private final ResponseFileProvider responseFileProvider;

    @Override
    public String getSupportedDomain() {
        return "response";
    }

    @Override
    public Response getDomainOrThrow(Long domainId) {
        return responseProvider.findById(domainId)
                .orElseThrow(() -> new GeneralException(ResponseErrorCode.RESPONSE_NOT_FOUND));
    }

    @Override
    public void validateWriter(Long memberId, Response Response) {
        if (!Response.getMember().getId().equals(memberId)) {
            throw new GeneralException(ResponseErrorCode.USER_NOT_WRITE_RESPONSE);
        }
    }

    @Override
    public ResponseFile toEntity(String fileName, String url, Response response) {
        return ResponseFile.create(fileName, url, response);
    }

    @Override
    public List<ResponseFile> toEntities(List<String> urls, List<String> names, Response domain) {
        if (urls == null || urls.isEmpty()) { return List.of(); }

        return IntStream.range(0, urls.size())
                .mapToObj(i -> ResponseFile.create(urls.get(i), names.get(i), domain))
                .toList();
    }

    @Override
    public void saveAll(List<ResponseFile> entities) {
        responseFileProvider.saveAll(entities);
    }

    @Override
    public ResponseFile getFileOrThrow(Long fileId) {
        return responseFileProvider.findById(fileId)
                .orElseThrow(() -> new GeneralException(ResponseErrorCode.RESPONSE_FILE_NOT_FOUND));
    }

    @Override
    public void validateFileUploader(Long memberId, ResponseFile file) {
        if (!file.getResponse().getMember().getId().equals(memberId)) {
            throw new GeneralException(ResponseErrorCode.USER_NOT_UPLOAD_FILE);
        }
    }
}
