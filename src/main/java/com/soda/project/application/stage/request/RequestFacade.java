package com.soda.project.application.stage.request;

import com.soda.member.entity.Member;
import com.soda.member.service.MemberService;
import com.soda.project.application.validator.ProjectValidator;
import com.soda.project.domain.stage.Stage;
import com.soda.project.domain.stage.StageService;
import com.soda.project.domain.stage.request.RequestService;
import com.soda.project.domain.stage.request.dto.RequestCreateRequest;
import com.soda.project.domain.stage.request.dto.RequestCreateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RequestFacade {
    private final RequestService requestService;
    private final MemberService memberService;
    private final StageService stageService;

    private final ProjectValidator projectValidator;

    public RequestCreateResponse createRequest(Long memberId, RequestCreateRequest requestCreateRequest) {
        Member member = memberService.getMemberWithProjectOrThrow(memberId);
        Stage stage = stageService.getStageOrThrow(requestCreateRequest.getStageId());
        projectValidator.validateProjectAuthority(member, requestCreateRequest.getProjectId());

        return requestService.createRequest(member, stage, requestCreateRequest);
    }
}
