package com.soda.request.service;

import com.soda.common.link.dto.LinkUploadRequest;
import com.soda.global.response.CommonErrorCode;
import com.soda.global.response.GeneralException;
import com.soda.member.entity.Member;
import com.soda.member.enums.MemberProjectRole;
import com.soda.member.enums.MemberRole;
import com.soda.member.repository.MemberRepository;
import com.soda.project.error.ProjectErrorCode;
import com.soda.request.dto.response.*;
import com.soda.request.entity.Request;
import com.soda.request.entity.Response;
import com.soda.request.entity.ResponseLink;
import com.soda.request.error.ResponseErrorCode;
import com.soda.request.repository.ResponseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ResponseService {
    private final RequestService requestService;

    private final ResponseRepository responseRepository;
    private final MemberRepository memberRepository;


    @Transactional
    public RequestApproveResponse approveRequest(Long memberId, Long requestId, RequestApproveRequest requestApproveRequest) {
        Member member = getMemberWithProjectOrThrow(memberId);
        Request request = requestService.getRequestOrThrow(requestId);

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
        Request request = requestService.getRequestOrThrow(requestId);

        validateProjectAuthority(member, requestRejectRequest.getProjectId());

        Response rejection = createResponse(member, request, requestRejectRequest.getComment(), requestRejectRequest.getLinks());
        responseRepository.save(rejection);

        requestService.reject(request);

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

    @Transactional
    public ResponseUpdateResponse updateRequest(Long memberId, Long responseId, ResponseUpdateRequest responseUpdateRequest) throws GeneralException {
        Response response = getResponseOrThrow(responseId);

        // update요청을 한 member가 Response를 작성했던 member인지 확인
        validateResponseWriter(response, memberId);

        // response의 제목, 내용을 수정
        updateResponseFields(responseUpdateRequest, response);

        responseRepository.save(response);
        responseRepository.flush();

        return ResponseUpdateResponse.fromEntity(response);
    }

    @Transactional
    public ResponseDeleteResponse deleteResponse(Long memberId, Long responseId) throws GeneralException {
        Response response = getResponseOrThrow(responseId);

        // delete요청을 한 member가 승인요청을 작성했던 member인지 확인
        validateResponseWriter(response, memberId);

        // request 소프트 삭제
        response.delete();

        return ResponseDeleteResponse.fromEntity(response);
    }


    // 분리한 메서드들
    private Response createResponse(Member member, Request request, String comment, List<LinkUploadRequest.LinkUploadDTO> linkDTOs) {
        Response response = buildResponse(member, request, comment);
        List<ResponseLink> links = buildResponseLinks(linkDTOs);
        response.addLinks(links);
        return response;
    }

    private Response buildResponse(Member member, Request request, String comment) {
        return Response.builder()
                .member(member)
                .request(request)
                .comment(comment)
                .build();
    }

    private List<ResponseLink> buildResponseLinks(List<LinkUploadRequest.LinkUploadDTO> linkDTOs) {
        if (linkDTOs == null) return List.of();

        return linkDTOs.stream()
                .map(dto -> ResponseLink.builder()
                        .urlAddress(dto.getUrlAddress())
                        .urlDescription(dto.getUrlDescription())
                        .build())
                .toList();
    }

    private void updateResponseFields(ResponseUpdateRequest responseUpdateRequest, Response response) {
        if(responseUpdateRequest.getComment() != null) {
            response.updateComment(responseUpdateRequest.getComment());
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

    private Member getMemberWithProjectOrThrow(Long memberId) {
        return memberRepository.findWithProjectsById(memberId)
                .orElseThrow(() -> new GeneralException(ProjectErrorCode.MEMBER_NOT_FOUND));
    }

    private void validateProjectAuthority(Member member, Long projectId) {
        if (!isCliInCurrentProject(projectId, member) && !isAdmin(member.getRole())) {
            throw new GeneralException(CommonErrorCode.USER_NOT_IN_PROJECT_CLI);
        }
    }

    // member가 현재 프로젝트에 속한 "개발사"의 멤버인지 확인하는 메서드
    private static boolean isCliInCurrentProject(Long projectId, Member member) {
        return member.getMemberProjects().stream()
                .anyMatch(mp ->
                        mp.getProject().getId().equals(projectId) &&
                                (mp.getRole() == MemberProjectRole.CLI_MANAGER || mp.getRole() == MemberProjectRole.CLI_PARTICIPANT)
                );
    }

    private static boolean isAdmin(MemberRole memberRole) {
        return memberRole == MemberRole.ADMIN;
    }
}
