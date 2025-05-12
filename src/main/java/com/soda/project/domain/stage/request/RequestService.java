package com.soda.project.domain.stage.request;

import com.soda.global.response.GeneralException;
import com.soda.member.domain.member.Member;
import com.soda.project.domain.stage.Stage;
import com.soda.project.interfaces.stage.request.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequestService {

    private final RequestFactory requestFactory;

    private final RequestProvider requestProvider;


    public RequestCreateResponse createRequest(Member member, Stage stage, RequestCreateRequest requestCreateRequest) {
        Request request = requestFactory.createRequest(member, stage, requestCreateRequest);
        requestProvider.store(request);
        return RequestCreateResponse.fromEntity(request);
    }

    public RequestCreateResponse createReRequest(Long requestId, Member member, Stage stage, ReRequestCreateRequest reRequestCreateRequest) {
        Request reRequest = requestFactory.createReRequest(requestId, member, stage, reRequestCreateRequest);
        requestProvider.store(reRequest);
        return RequestCreateResponse.fromEntity(reRequest);
    }

    public RequestUpdateResponse updateRequest(Request request, RequestUpdateRequest requestUpdateRequest) {
        Request updatedRequest = requestFactory.updateRequest(request, requestUpdateRequest);
        requestProvider.store(updatedRequest);
        return RequestUpdateResponse.fromEntity(updatedRequest);
    }

    public RequestDeleteResponse deleteRequest(Request request) {
        request.delete();
        return RequestDeleteResponse.fromEntity(request);
    }

    public Page<RequestDTO> findRequests(Long projectId, GetRequestCondition condition, Pageable pageable) {
        return requestProvider.searchByCondition(projectId, condition, pageable)
                .map(RequestDTO::fromEntity);
    }

    public Page<RequestDTO> findMemberRequests(Long memberId, GetMemberRequestCondition condition, Pageable pageable) {
        return requestProvider.searchByMemberCondition(memberId, condition, pageable)
                .map(RequestDTO::fromEntity);
    }

    public List<RequestDTO> findAllByStageId(Long stageId) {
        return requestProvider.findAllByStage_IdAndIsDeletedFalse(stageId).stream()
                .map(RequestDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public RequestDTO findById(Long requestId) {
        return RequestDTO.fromEntity(getRequestOrThrow(requestId));
    }

    public Request getRequestOrThrow(Long requestId) {
        return requestProvider.findById(requestId).orElseThrow(() -> new GeneralException(RequestErrorCode.REQUEST_NOT_FOUND));
    }
}
