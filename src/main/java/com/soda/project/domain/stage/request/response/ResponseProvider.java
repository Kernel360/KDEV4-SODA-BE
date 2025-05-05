package com.soda.project.domain.stage.request.response;

import com.soda.project.interfaces.stage.request.response.dto.ResponseDTO;

import java.util.List;
import java.util.Optional;

public interface ResponseProvider {
    Response store(Response response);

    Response storeAndflush(Response response);

    List<ResponseDTO> findAllByRequestId(Long requestId);

    Response getResponseOrThrow(Long responseId);

    Long countNotDeletedByRequestId(Response response);

    Optional<Response> findById(Long responseId);
}
