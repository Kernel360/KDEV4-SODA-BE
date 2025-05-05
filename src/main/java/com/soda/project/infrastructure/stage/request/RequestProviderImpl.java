package com.soda.project.infrastructure.stage.request;

import com.soda.project.domain.stage.request.Request;
import com.soda.project.domain.stage.request.RequestProvider;
import com.soda.project.domain.stage.request.dto.GetMemberRequestCondition;
import com.soda.project.domain.stage.request.dto.GetRequestCondition;
import com.soda.project.infrastructure.RequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RequestProviderImpl implements RequestProvider {
    private final RequestRepository requestRepository;

    @Override
    public Request store(Request request) {
        return requestRepository.save(request);
    }

    @Override
    public Page<Request> searchByCondition(Long projectId, GetRequestCondition condition, Pageable pageable) {
        return requestRepository.searchByCondition(projectId, condition, pageable);
    }

    @Override
    public Page<Request> searchByMemberCondition(Long memberId, GetMemberRequestCondition condition, Pageable pageable) {
        return requestRepository.searchByMemberCondition(memberId, condition, pageable);
    }

    @Override
    public List<Request> findAllByStage_IdAndIsDeletedFalse(Long stageId) {
        return requestRepository.findAllByStage_IdAndIsDeletedFalse(stageId);
    }
}