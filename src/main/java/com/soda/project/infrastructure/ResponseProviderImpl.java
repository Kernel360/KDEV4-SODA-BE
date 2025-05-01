package com.soda.project.infrastructure;

import com.soda.project.domain.stage.request.response.Response;
import com.soda.project.domain.stage.request.response.ResponseProvider;

public class ResponseProviderImpl implements ResponseProvider {
    private ResponseRepository responseRepository;

    @Override
    public Response store(Response response) {
        /* 필요시 response 내부 필드 유무 확인해서 Exception Throwing하는 로직 추가 가능 */
        return responseRepository.save(response);
    }
}