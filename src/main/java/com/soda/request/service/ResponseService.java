package com.soda.request.service;

import com.soda.global.response.CommonErrorCode;
import com.soda.global.response.GeneralException;
import com.soda.member.entity.Member;
import com.soda.member.enums.MemberProjectRole;
import com.soda.member.enums.MemberRole;
import com.soda.member.repository.MemberRepository;
import com.soda.project.error.ProjectErrorCode;
import com.soda.request.dto.*;
import com.soda.request.entity.Request;
import com.soda.request.entity.RequestLink;
import com.soda.request.entity.Response;
import com.soda.request.entity.ResponseLink;
import com.soda.request.repository.RequestRepository;
import com.soda.request.repository.ResponseLinkRepository;
import com.soda.request.repository.ResponseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ResponseService {
    private final RequestRepository requestRepository;
    private final ResponseRepository responseRepository;
    private final MemberRepository memberRepository;


    @Transactional
    public RequestApproveResponse approveRequest(Long memberId, Long requestId, RequestApproveRequest requestApproveRequest) {
        Member member = getMemberWithProjectOrThrow(memberId);
        Request request = getRequestOrThrow(requestId);

        validateProjectAuthority(member, requestApproveRequest.getProjectId());

        Response approval = createResponse(member, request, requestApproveRequest.getComment(), requestApproveRequest.getLinks());
        responseRepository.save(approval);

        request.approve();

        return RequestApproveResponse.fromEntity(approval);
    }

    @Transactional
    public RequestRejectResponse rejectRequest(Long memberId, Long requestId, RequestRejectRequest requestRejectRequest) {
        Member member = getMemberWithProjectOrThrow(memberId);
        Request request = getRequestOrThrow(requestId);

        validateProjectAuthority(member, requestRejectRequest.getProjectId());

        Response rejection = createResponse(member, request, requestRejectRequest.getComment(), requestRejectRequest.getLinks());
        responseRepository.save(rejection);

        request.reject();

        return RequestRejectResponse.fromEntity(rejection);
    }

    // 분리한 메서드들
    private Member getMemberWithProjectOrThrow(Long memberId) {
        return memberRepository.findWithProjectsById(memberId)
                .orElseThrow(() -> new GeneralException(ProjectErrorCode.MEMBER_NOT_FOUND));
    }

    private Request getRequestOrThrow(Long requestId) {
        return requestRepository.findById(requestId).orElseThrow(() -> new GeneralException(CommonErrorCode.REQUEST_NOT_FOUND));
    }

    private void validateProjectAuthority(Member member, Long projectId) {
        if (!isCliInCurrentProject(projectId, member) && !isAdmin(member)) {
            throw new GeneralException(CommonErrorCode.USER_NOT_IN_PROJECT_CLI);
        }
    }

    private Response createResponse(Member member, Request request, String comment, List<ResponseLinkDTO> linkDTOs) {
        Response response = Response.builder()
                .member(member)
                .request(request)
                .comment(comment)
                .build();

        List<ResponseLink> links = linkDTOs.stream()
                .map(linkDto -> ResponseLink.builder()
                        .urlAddress(linkDto.getUrlAddress())
                        .urlDescription(linkDto.getUrlDescription())
                        .response(response)
                        .build())
                .toList();

        response.updateLink(links);
        return response;
    }

    // member가 현재 프로젝트에 속한 "개발사"의 멤버인지 확인하는 메서드
    private static boolean isCliInCurrentProject(Long projectId, Member member) {
        return member.getMemberProjects().stream()
                .anyMatch(mp ->
                        mp.getProject().getId().equals(projectId) &&
                                (mp.getRole() == MemberProjectRole.CLI_MANAGER || mp.getRole() == MemberProjectRole.CLI_PARTICIPANT)
                );
    }

    private static boolean isAdmin(Member member) {
        return member.getRole() == MemberRole.ADMIN;
    }

}
