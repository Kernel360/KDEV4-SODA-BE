package com.soda.project.application.stage.request.response;

import com.soda.member.entity.Member;
import com.soda.member.service.MemberService;
import com.soda.project.application.stage.request.response.validator.ResponseValidator;
import com.soda.project.application.stage.request.validator.RequestApproverValidator;
import com.soda.project.application.validator.ProjectValidator;
import com.soda.project.domain.stage.request.Request;
import com.soda.project.domain.stage.request.RequestService;
import com.soda.project.domain.stage.request.response.Response;
import com.soda.project.domain.stage.request.response.ResponseService;
import com.soda.project.domain.stage.request.response.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResponseFacade {

    private final ResponseService responseService;
    private final MemberService memberService;
    private final RequestService requestService;

    private final ProjectValidator projectValidator;
    private final RequestApproverValidator requestApproverValidator;
    private final ResponseValidator responseValidator;

    public RequestApproveResponse approveRequest(Long memberId, Long requestId,
                                                 RequestApproveRequest requestApproveRequest) {
        Member member = memberService.getMemberWithProjectOrThrow(memberId);
        Request request = requestService.getRequestOrThrow(requestId);
        projectValidator.validateProjectAuthority(member, requestApproveRequest.getProjectId());
        requestApproverValidator.validateApprover(member, request.getApprovers());

        return responseService.approveRequest(member, request, requestApproveRequest);
    }

    public RequestRejectResponse rejectRequest(Long memberId, Long requestId,
                                               RequestRejectRequest requestRejectRequest) {
        Member member = memberService.getMemberWithProjectOrThrow(memberId);
        Request request = requestService.getRequestOrThrow(requestId);
        projectValidator.validateProjectAuthority(member, requestRejectRequest.getProjectId());
        requestApproverValidator.validateApprover(member, request.getApprovers());

        return responseService.rejectRequest(member, request, requestRejectRequest);
    }

    public List<ResponseDTO> findAllByRequestId(Long requestId) {
        return responseService.findAllByRequestId(requestId);
    }

    public ResponseDTO findById(Long responseId) {
        return responseService.findById(responseId);
    }

    public ResponseUpdateResponse updateResponse(Long memberId, Long responseId,
                                                 ResponseUpdateRequest responseUpdateRequest) {
        Response response = responseService.getResponseOrThrow(responseId);
        responseValidator.validateResponseWriter(response, memberId);
        return responseService.updateResponse(response, responseUpdateRequest);
    }

    public ResponseDeleteResponse deleteResponse(Long memberId, Long responseId) {
        Response response = responseService.getResponseOrThrow(responseId);
        responseValidator.validateResponseWriter(response, memberId);

        return responseService.deleteResponse(response);
    }
}
