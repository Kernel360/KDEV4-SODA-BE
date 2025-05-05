package com.soda.project.domain.stage.request;

import com.soda.common.link.service.LinkService;
import com.soda.global.response.GeneralException;
import com.soda.member.domain.member.MemberProvider;
import com.soda.member.entity.Member;
import com.soda.member.error.MemberErrorCode;
import com.soda.project.domain.stage.Stage;
import com.soda.project.interfaces.stage.request.dto.MemberAssignDTO;
import com.soda.project.interfaces.stage.request.dto.ReRequestCreateRequest;
import com.soda.project.interfaces.stage.request.dto.RequestCreateRequest;
import com.soda.project.interfaces.stage.request.dto.RequestUpdateRequest;
import com.soda.project.domain.stage.request.link.RequestLink;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RequestFactory {

    private final LinkService linkService;

    private final MemberProvider memberProvider;

    public Request createRequest(Member member, Stage stage, RequestCreateRequest requestCreateRequest) {
        Request request = Request.createRequest(member, stage, requestCreateRequest);

        List<RequestLink> links = linkService.buildLinks("request", request, requestCreateRequest.getLinks());
        request.addLinks(links);

        designateApprover(requestCreateRequest.getMembers(), request);

        return request;
    }

    public Request createReRequest(Long requestId, Member member, Stage stage, ReRequestCreateRequest reRequestCreateRequest) {
        Request reRequest = Request.createReRequest(requestId, member, stage, reRequestCreateRequest);

        List<RequestLink> links = linkService.buildLinks("request", reRequest, reRequestCreateRequest.getLinks());
        reRequest.addLinks(links);

        designateApprover(reRequestCreateRequest.getMembers(), reRequest);

        return reRequest;
    }

    private void designateApprover(List<MemberAssignDTO> dtos, Request request) {
        List<Long> memberIds = dtos.stream()
                .map(MemberAssignDTO::getId)
                .collect(Collectors.toList());

        List<Member> approvers = memberProvider.findAllById(memberIds);

        if (approvers.size() != memberIds.size()) {
            throw new GeneralException(MemberErrorCode.NOT_FOUND_MEMBER);
        }

        request.addApprovers(ApproverDesignation.designateApprover(request, approvers));
    }

    public Request updateRequest(Request request, RequestUpdateRequest requestUpdateRequest) {
        request.updateContents(requestUpdateRequest.getTitle(), requestUpdateRequest.getContent());
        if(requestUpdateRequest.getLinks() != null) {
            request.addLinks(linkService.buildLinks("request", request, requestUpdateRequest.getLinks()));
        }
        if(requestUpdateRequest.getMembers() != null) {
            designateApprover(requestUpdateRequest.getMembers(), request);
        }
        return request;
    }
}
