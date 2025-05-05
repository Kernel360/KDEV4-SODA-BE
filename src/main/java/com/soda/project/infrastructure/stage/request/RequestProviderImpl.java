package com.soda.project.infrastructure.stage.request;

import com.soda.project.domain.stage.request.Request;
import com.soda.project.domain.stage.request.RequestProvider;
import com.soda.project.infrastructure.RequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RequestProviderImpl implements RequestProvider {
    private final RequestRepository requestRepository;

    @Override
    public Request store(Request request) {
        return requestRepository.save(request);
    }
}