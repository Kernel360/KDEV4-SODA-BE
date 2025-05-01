package com.soda.project.domain.stage.request.response;

import com.soda.project.domain.stage.request.response.dto.ResponseDTO;

import java.util.List;

public interface ResponseProvider {
    Response store(Response response);

    Response storeAndflush(Response response);

    List<ResponseDTO> findAllByRequestId(Long requestId);

    Response findById(Long responseId);

    Long countNotDeletedByRequestId(Response response);
}
