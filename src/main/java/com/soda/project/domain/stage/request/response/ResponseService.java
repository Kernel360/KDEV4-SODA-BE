package com.soda.project.domain.stage.request.response;

import com.soda.common.link.dto.LinkUploadRequest;
import com.soda.common.link.service.LinkService;
import com.soda.global.log.data.annotation.LoggableEntityAction;
import com.soda.global.response.CommonErrorCode;
import com.soda.global.response.GeneralException;
import com.soda.member.entity.Member;
import com.soda.project.domain.member.enums.MemberProjectRole;
import com.soda.member.enums.MemberRole;
import com.soda.member.repository.MemberRepository;
import com.soda.project.domain.error.ProjectErrorCode;
import com.soda.project.domain.stage.request.ApproverDesignation;
import com.soda.project.domain.stage.request.Request;
import com.soda.project.domain.stage.request.RequestService;
import com.soda.project.domain.stage.request.error.RequestErrorCode;
import com.soda.project.domain.stage.request.response.dto.*;
import com.soda.project.domain.stage.request.response.enums.ResponseStatus;
import com.soda.project.domain.stage.request.response.error.ResponseErrorCode;
import com.soda.project.domain.stage.request.response.link.ResponseLink;
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
    private final RequestService requestService;

    private final ResponseRepository responseRepository;
    private final MemberRepository memberRepository;
    private final LinkService linkService;


    @LoggableEntityAction(action = "CREATE", entityClass = Response.class)
    @Transactional
    public RequestApproveResponse approveRequest(Long memberId, Long requestId, RequestApproveRequest requestApproveRequest) {
        Member member = getMemberWithProjectOrThrow(memberId);
        Request request = requestService.getRequestOrThrow(requestId);

        validateProjectAuthority(member, requestApproveRequest.getProjectId());
        validateApprover(member, request.getApprovers());

        Response approval = createResponse(member, request, requestApproveRequest.getComment(), requestApproveRequest.getLinks(), ResponseStatus.APPROVED);
        responseRepository.save(approval);

        requestService.approve(request);

        return RequestApproveResponse.fromEntity(approval);
    }

    @LoggableEntityAction(action = "CREATE", entityClass = Response.class)
    @Transactional
    public RequestRejectResponse rejectRequest(Long memberId, Long requestId, RequestRejectRequest requestRejectRequest) {
        Member member = getMemberWithProjectOrThrow(memberId);
        Request request = requestService.getRequestOrThrow(requestId);

        validateProjectAuthority(member, requestRejectRequest.getProjectId());
        validateApprover(member, request.getApprovers());

        Response rejection = createResponse(member, request, requestRejectRequest.getComment(), requestRejectRequest.getLinks(), ResponseStatus.REJECTED);
        responseRepository.save(rejection);

        requestService.reject(request);

        return RequestRejectResponse.fromEntity(rejection);
    }

    private void validateApprover(Member member, List<ApproverDesignation> approvers) {
        List<Member> members = approvers.stream()
                .map(ApproverDesignation::getMember)
                .collect(Collectors.toList());
        if (!members.contains(member)) {
            throw new GeneralException(RequestErrorCode.USER_IS_NOT_APPROVER);
        }
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
    private Response createResponse(Member member, Request request, String comment, List<LinkUploadRequest.LinkUploadDTO> linkDTOs, ResponseStatus responseStatus) {
        Response response = buildResponse(member, request, comment, responseStatus);
        List<ResponseLink> links = linkService.buildLinks("response", response, linkDTOs);
        response.addLinks(links);
        return response;
    }

    private Response buildResponse(Member member, Request request, String comment, ResponseStatus responseStatus) {
        return Response.builder()
                .member(member)
                .request(request)
                .comment(comment)
                .status(responseStatus)
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
        if(responseUpdateRequest.getLinks() != null) {
            response.addLinks(linkService.buildLinks("response", response, responseUpdateRequest.getLinks()));
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
