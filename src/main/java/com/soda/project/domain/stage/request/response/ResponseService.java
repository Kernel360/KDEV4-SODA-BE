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
import java.util.stream.Collectors;

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
        responseProvider.store(approval);

        return RequestApproveResponse.fromEntity(approval);
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
        responseProvider.store(rejection);

        return RequestRejectResponse.fromEntity(rejection);
    }

    public List<ResponseDTO> findAllByRequestId(Long requestId) {
        return responseRepository.findAllByRequest_IdAndIsDeletedFalse(requestId).stream()
                .map(ResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public ResponseDTO findById(Long responseId) {
        return ResponseDTO.fromEntity(getResponseOrThrow(responseId));
    }

    @LoggableEntityAction(action = "UPDATE", entityClass = Response.class)
    @Transactional
    public ResponseUpdateResponse updateResponse(Long memberId, Long responseId, ResponseUpdateRequest responseUpdateRequest) {
        Response response = getResponseOrThrow(responseId);

        // update요청을 한 member가 Response를 작성했던 member인지 확인
        validateResponseWriter(response, memberId);

        // response의 제목, 내용을 수정
        updateResponseFields(responseUpdateRequest, response);

        responseRepository.save(response);
        responseRepository.flush();

        return ResponseUpdateResponse.fromEntity(response);
    }

    @LoggableEntityAction(action = "DELETE", entityClass = Response.class)
    @Transactional
    public ResponseDeleteResponse deleteResponse(Long memberId, Long responseId) throws GeneralException {
        Response response = getResponseOrThrow(responseId);

        // delete요청을 한 member가 승인요청을 작성했던 member인지 확인
        validateResponseWriter(response, memberId);

        // request 소프트 삭제
        response.delete();

        checkAndchangeStatusToPending(response);

        return ResponseDeleteResponse.fromEntity(response);
    }

    private void checkAndchangeStatusToPending(Response response) {
        if(responseRepository.countNotDeletedByRequestId(response.getRequest().getId()) == 0) {
            requestService.changeStatusToPending(response.getRequest());
        }
    }


    // 분리한 메서드들

    private void updateResponseFields(ResponseUpdateRequest responseUpdateRequest, Response response) {
        if(responseUpdateRequest.getComment() != null) {
            response.updateComment(responseUpdateRequest.getComment());
        }
        if(responseUpdateRequest.getLinks() != null) {
            response.addLinks(linkService.buildLinks(DOMAIN_TYPE, response, responseUpdateRequest.getLinks()));
        }
    }

    private Response getResponseOrThrow(Long responseId) {
        return responseRepository.findById(responseId).orElseThrow(() -> new GeneralException(ResponseErrorCode.RESPONSE_NOT_FOUND));
    }

    // 외부 메서드(외부로 옮겨야함)
    private void validateResponseWriter(Response response, Long memberId) throws GeneralException {
        boolean isRequestWriter = response.getMember().getId().equals(memberId);
        if (!isRequestWriter) { throw new GeneralException(ResponseErrorCode.USER_NOT_WRITE_RESPONSE); }
    }
}
