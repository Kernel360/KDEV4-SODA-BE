package com.soda.project.infrastructure.stage.request.response;

import com.soda.global.response.GeneralException;
import com.soda.project.domain.stage.request.response.Response;
import com.soda.project.domain.stage.request.response.ResponseProvider;
import com.soda.project.interfaces.stage.request.response.dto.ResponseDTO;
import com.soda.project.domain.stage.request.response.ResponseErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ResponseProviderImpl implements ResponseProvider {
    private final ResponseRepository responseRepository;

    @Override
    public Response store(Response response) {
        /* 필요시 response 내부 필드 유무 확인해서 Exception Throwing하는 로직 추가 가능 */
        return responseRepository.save(response);
    }

    @Override
    public Response storeAndflush(Response response) {
        Response savedResponse = responseRepository.save(response);
        responseRepository.flush();
        return savedResponse;
    }

    @Override
    public List<ResponseDTO> findAllByRequestId(Long requestId) {
        return responseRepository.findAllByRequest_IdAndIsDeletedFalse(requestId).stream()
                .map(ResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Response getResponseOrThrow(Long responseId) {
        return responseRepository.findById(responseId).orElseThrow(() -> new GeneralException(ResponseErrorCode.RESPONSE_NOT_FOUND));
    }

    @Override
    public Long countNotDeletedByRequestId(Response response) {
        return responseRepository.countNotDeletedByRequestId(response.getRequest().getId());
    }

    @Override
    public Optional<Response> findById(Long responseId) {
        return responseRepository.findById(responseId);
    }
}