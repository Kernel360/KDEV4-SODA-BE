package com.soda.project.application.stage.request.response;

import com.soda.member.entity.Member;
import com.soda.member.service.MemberService;
import com.soda.project.application.stage.request.validator.RequestApproverValidator;
import com.soda.project.application.validator.ProjectValidator;
import com.soda.project.domain.stage.request.Request;
import com.soda.project.domain.stage.request.RequestService;
import com.soda.project.domain.stage.request.response.ResponseService;
import com.soda.project.domain.stage.request.response.dto.RequestApproveRequest;
import com.soda.project.domain.stage.request.response.dto.RequestApproveResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResponseFacade {

    private final ResponseService responseService;
    private final MemberService memberService;
    private final RequestService requestService;

    private final ProjectValidator projectValidator;
    private final RequestApproverValidator requestApproverValidator;

    public RequestApproveResponse approveRequest(Long memberId, Long requestId, RequestApproveRequest requestApproveRequest) {
        Member member = memberService.getMemberWithProjectOrThrow(memberId);
        Request request = requestService.getRequestOrThrow(requestId);
        projectValidator.validateProjectAuthority(member, requestApproveRequest.getProjectId());
        requestApproverValidator.validateApprover(member, request.getApprovers());

        return responseService.approveRequest(member, request, requestApproveRequest);
    }
}
