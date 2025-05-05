package com.soda.project.application.stage.request;

import com.soda.global.log.data.annotation.LoggableEntityAction;
import com.soda.member.entity.Member;
import com.soda.member.service.MemberService;
import com.soda.project.application.validator.ProjectValidator;
import com.soda.project.domain.stage.Stage;
import com.soda.project.domain.stage.StageService;
import com.soda.project.domain.stage.request.Request;
import com.soda.project.domain.stage.request.RequestService;
import com.soda.project.domain.stage.request.dto.ReRequestCreateRequest;
import com.soda.project.domain.stage.request.dto.RequestCreateRequest;
import com.soda.project.domain.stage.request.dto.RequestCreateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RequestFacade {
    private final RequestService requestService;
    private final MemberService memberService;
    private final StageService stageService;

    private final ProjectValidator projectValidator;
    private final RequestValidator requestValidator;

    @LoggableEntityAction(action = "CREATE", entityClass = Request.class)
    @Transactional
    public RequestCreateResponse createRequest(Long memberId, RequestCreateRequest requestCreateRequest) {
        Member member = memberService.getMemberWithProjectOrThrow(memberId);
        Stage stage = stageService.getStageOrThrow(requestCreateRequest.getStageId());
        projectValidator.validateProjectAuthority(member, requestCreateRequest.getProjectId());

        return requestService.createRequest(member, stage, requestCreateRequest);
    }

    @LoggableEntityAction(action = "CREATE", entityClass = Request.class)
    @Transactional
    public RequestCreateResponse createReRequest(Long memberId, Long requestId, ReRequestCreateRequest reRequestCreateRequest) {
        Member member = memberService.getMemberWithProjectOrThrow(memberId);
        Request parentRequest = requestService.getRequestOrThrow(requestId);
        Stage stage = stageService.getStageOrThrow(parentRequest.getStage().getId());
        projectValidator.validateProjectAuthority(member, requestId);
        requestValidator.validateRequestStatus(parentRequest);

        return requestService.createReRequest(requestId, member, stage, reRequestCreateRequest);
    }
}
