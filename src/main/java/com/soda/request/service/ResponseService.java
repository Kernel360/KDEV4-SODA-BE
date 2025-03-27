package com.soda.request.service;

import com.soda.global.response.CommonErrorCode;
import com.soda.global.response.GeneralException;
import com.soda.member.entity.Member;
import com.soda.member.enums.MemberProjectRole;
import com.soda.member.enums.MemberRole;
import com.soda.member.repository.MemberRepository;
import com.soda.project.error.ProjectErrorCode;
import com.soda.request.dto.link.LinkDTO;
import com.soda.request.dto.response.*;
import com.soda.request.entity.Request;
import com.soda.request.entity.Response;
import com.soda.request.entity.ResponseLink;
import com.soda.request.error.RequestErrorCode;
import com.soda.request.repository.RequestRepository;
import com.soda.request.repository.ResponseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ResponseService {
    private final RequestService requestService;

    private final RequestRepository requestRepository;
    private final ResponseRepository responseRepository;
    private final MemberRepository memberRepository;


    @Transactional
    public RequestApproveResponse approveRequest(Long memberId, Long requestId, RequestApproveRequest requestApproveRequest) {
        Member member = getMemberWithProjectOrThrow(memberId);
        Request request = getRequestOrThrow(requestId);

        // 본 메서드 호출의 주체인 멤버가 승인할 권한이 있는지 확인
        validateProjectAuthority(member, requestApproveRequest.getProjectId());

        // Request(승인요청)에 대한 Response(응답-승인) 데이터를 생성 및 저장
        Response approval = createResponse(member, request, requestApproveRequest.getComment(), requestApproveRequest.getLinks());
        responseRepository.save(approval);

        // Request(승인요청)의 상태를 'APPROVED' 변경
        requestService.approve(request);

        return RequestApproveResponse.fromEntity(approval);
    }

    @Transactional
    public RequestRejectResponse rejectRequest(Long memberId, Long requestId, RequestRejectRequest requestRejectRequest) {
        Member member = getMemberWithProjectOrThrow(memberId);
        Request request = getRequestOrThrow(requestId);

        validateProjectAuthority(member, requestRejectRequest.getProjectId());

        Response rejection = createResponse(member, request, requestRejectRequest.getComment(), requestRejectRequest.getLinks());
        responseRepository.save(rejection);

        requestService.reject(request);

        return RequestRejectResponse.fromEntity(rejection);
    }


    // 분리한 메서드들
    private Member getMemberWithProjectOrThrow(Long memberId) {
        return memberRepository.findWithProjectsById(memberId)
                .orElseThrow(() -> new GeneralException(ProjectErrorCode.MEMBER_NOT_FOUND));
    }

    private Request getRequestOrThrow(Long requestId) {
        return requestRepository.findById(requestId).orElseThrow(() -> new GeneralException(RequestErrorCode.REQUEST_NOT_FOUND));
    }

    private void validateProjectAuthority(Member member, Long projectId) {
        if (!isCliInCurrentProject(projectId, member) && !isAdmin(member)) {
            throw new GeneralException(CommonErrorCode.USER_NOT_IN_PROJECT_CLI);
        }
    }

    private Response createResponse(Member member, Request request, String comment, List<LinkDTO> linkDTOs) {
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
