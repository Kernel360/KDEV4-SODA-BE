package com.soda.project.domain.stage.request.response;

import com.soda.common.link.service.LinkService;
import com.soda.global.log.data.annotation.LoggableEntityAction;
import com.soda.global.response.GeneralException;
import com.soda.member.entity.Member;
import com.soda.member.service.MemberService;
import com.soda.project.domain.stage.request.Request;
import com.soda.project.domain.stage.request.RequestService;
import com.soda.project.domain.stage.request.response.dto.*;
import com.soda.project.domain.stage.request.response.error.ResponseErrorCode;
import com.soda.project.infrastructure.ResponseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ResponseService {
    private final static String DOMAIN_TYPE = "response";

    private final RequestService requestService;

    private final ResponseRepository responseRepository;
    private final MemberService memberService;
    private final LinkService linkService;

    private final ResponseFactory responseFactory;

    private final ResponseProvider responseProvider;


    @LoggableEntityAction(action = "CREATE", entityClass = Response.class)
    @Transactional
    public RequestApproveResponse approveRequest(Member member, Request request, RequestApproveRequest requestApproveRequest) {
        Response approval = responseFactory.createApproveResponse(
                member,
                request,
                requestApproveRequest.getComment(),
                requestApproveRequest.getLinks()
        );
        return RequestApproveResponse.fromEntity(responseProvider.store(approval));
    }

    @LoggableEntityAction(action = "CREATE", entityClass = Response.class)
    @Transactional
    public RequestRejectResponse rejectRequest(Member member, Request request, RequestRejectRequest requestRejectRequest) {
        Response rejection = responseFactory.createRejectResponse(
                member,
                request,
                requestRejectRequest.getComment(),
                requestRejectRequest.getLinks()
        );
        return RequestRejectResponse.fromEntity(responseProvider.store(rejection));
    }

    public List<ResponseDTO> findAllByRequestId(Long requestId) {
        return responseProvider.findAllByRequestId(requestId);
    }

    public ResponseDTO findById(Long responseId) {
        return ResponseDTO.fromEntity(responseProvider.findById(responseId));
    }

    @LoggableEntityAction(action = "UPDATE", entityClass = Response.class)
    @Transactional
    public ResponseUpdateResponse updateResponse(Long memberId, Long responseId, ResponseUpdateRequest responseUpdateRequest) {
        Response response = responseFactory.updateResponse(
                memberId,
                responseId,
                responseUpdateRequest.getComment(),
                responseUpdateRequest.getLinks()
        );
        return ResponseUpdateResponse.fromEntity(responseProvider.storeAndflush(response));
    }

    @LoggableEntityAction(action = "DELETE", entityClass = Response.class)
    @Transactional
    public ResponseDeleteResponse deleteResponse(Response response) throws GeneralException {
        Long countResponse = responseProvider.countNotDeletedByRequestId(response);
        response.delete(countResponse);
        return ResponseDeleteResponse.fromEntity(response);
    }

    public Response getResponseOrThrow(Long responseId) {
        return responseRepository.findById(responseId).orElseThrow(() -> new GeneralException(ResponseErrorCode.RESPONSE_NOT_FOUND));
    }
}
