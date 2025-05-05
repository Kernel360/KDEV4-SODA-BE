package com.soda.project.domain.stage.request;

import com.soda.common.file.service.FileService;
import com.soda.common.file.service.S3Service;
import com.soda.common.link.service.LinkService;
import com.soda.global.log.data.annotation.LoggableEntityAction;
import com.soda.global.response.CommonErrorCode;
import com.soda.global.response.GeneralException;
import com.soda.member.entity.Member;
import com.soda.project.domain.member.enums.MemberProjectRole;
import com.soda.member.enums.MemberRole;
import com.soda.member.error.MemberErrorCode;
import com.soda.member.repository.MemberRepository;
import com.soda.project.domain.error.StageErrorCode;
import com.soda.project.domain.stage.Stage;
import com.soda.project.domain.stage.request.dto.*;
import com.soda.project.domain.stage.request.enums.RequestStatus;
import com.soda.project.domain.stage.request.error.RequestErrorCode;
import com.soda.project.domain.stage.request.link.RequestLink;
import com.soda.project.infrastructure.ApproverDesignationRepository;
import com.soda.project.infrastructure.RequestLinkRepository;
import com.soda.project.infrastructure.RequestRepository;
import com.soda.project.infrastructure.StageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RequestService {
    private final LinkService linkService;
    private final FileService fileService;
    private final S3Service s3Service;

    private final RequestFactory requestFactory;

    private final RequestProvider requestProvider;

    private final RequestRepository requestRepository;
    private final MemberRepository memberRepository;
    private final StageRepository stageRepository;
    private final RequestLinkRepository requestLinkRepository;
    private final ApproverDesignationRepository approverDesignationRepository;

    /*
    Request(승인요청) 생성
    어떤 member가 어떤 Stage에서 승인요청을 생성했는지를 request 데이터에 넣어줘야함.
    Request 데이터 생성 전에, 요청한 member가 현재 프로젝트에 속한 "개발사"의 멤버이거나 ADMIN유저인지 확인해야함.
    */
    public RequestCreateResponse createRequest(Member member, Stage stage, RequestCreateRequest requestCreateRequest) {
        Request request = requestFactory.createRequest(member, stage, requestCreateRequest);
        return RequestCreateResponse.fromEntity(requestProvider.store(request));
    }

    public RequestCreateResponse createReRequest(Long requestId, Member member, Stage stage, ReRequestCreateRequest reRequestCreateRequest) {
        Request reRequest = requestFactory.createReRequest(requestId, member, stage, reRequestCreateRequest);
        return RequestCreateResponse.fromEntity(requestProvider.store(reRequest));
    }

    private void validateRequestStatus(Request parentRequest) {
        if (parentRequest.getStatus() != RequestStatus.REJECTED) {
            throw new GeneralException(RequestErrorCode.REQUEST_NOT_REJECTED);
        }
    }

    public Page<RequestDTO> findRequests(Long projectId, GetRequestCondition condition, Pageable pageable) {
        return requestProvider.searchByCondition(projectId, condition, pageable)
                .map(RequestDTO::fromEntity);
    }

    public Page<RequestDTO> findMemberRequests(Long memberId, GetMemberRequestCondition condition, Pageable pageable) {
        return requestProvider.searchByMemberCondition(memberId, condition, pageable)
                .map(RequestDTO::fromEntity);
    }


    public List<RequestDTO> findAllByStageId(Long stageId) {
        return requestProvider.findAllByStage_IdAndIsDeletedFalse(stageId).stream()
                .map(RequestDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public RequestDTO findById(Long requestId) {
        return RequestDTO.fromEntity(getRequestOrThrow(requestId));
    }


    @LoggableEntityAction(action = "UPDATE", entityClass = Request.class)
    @Transactional
    public RequestUpdateResponse updateRequest(Long memberId, Long requestId, RequestUpdateRequest requestUpdateRequest) {
        Request request = getRequestOrThrow(requestId);

        // update요청을 한 member가 승인요청을 작성했던 member인지 확인
        validateRequestWriter(memberId, request);

        // request의 제목, 내용을 수정
        updateRequestFields(requestUpdateRequest, request);

        requestRepository.save(request);
        requestRepository.flush();

        return RequestUpdateResponse.fromEntity(request);
    }

    @LoggableEntityAction(action = "DELETE", entityClass = Request.class)
    @Transactional
    public RequestDeleteResponse deleteRequest(Long memberId, Long requestId) throws GeneralException {
        Request request = getRequestOrThrow(requestId);

        // delete요청을 한 member가 승인요청을 작성했던 member인지 확인
        validateRequestWriter(memberId, request);

        // request 소프트 삭제
        request.delete();

        return RequestDeleteResponse.fromEntity(request);
    }



    // 분리한 메서드들

    // isDevInCurrentProject에서 memberProject를 조회해 userDetails.getMember로 멤버객체를 그대로 사용하면 "LazyInitializationException"이 발생해
    // memberId를 바탕으로 (레프트)페치조인해 memberProject와 함께 영속성 컨텍스트에 등록
    private Member getMemberWithProjectOrThrow(Long memberId) {
        return memberRepository.findWithProjectsById(memberId)
                .orElseThrow(() -> new GeneralException(MemberErrorCode.NOT_FOUND_MEMBER));
    }

    private Stage getStageOrThrow(Long stageId) {
        return stageRepository.findById(stageId).orElseThrow(() -> new GeneralException(StageErrorCode.STAGE_NOT_FOUND));
    }

    public Request getRequestOrThrow(Long requestId) {
        return requestRepository.findById(requestId).orElseThrow(() -> new GeneralException(RequestErrorCode.REQUEST_NOT_FOUND));
    }

    private void validateProjectAuthority(Member member, Long projectId) {
        if (!isDevInCurrentProject(projectId, member) && !isAdmin(member.getRole())) {
            throw new GeneralException(CommonErrorCode.USER_NOT_IN_PROJECT_DEV);
        }
    }

    // member가 현재 프로젝트에 속한 "개발사"의 멤버인지 확인하는 메서드
    private boolean isDevInCurrentProject(Long projectId, Member member) {
        return member.getMemberProjects().stream()
                .anyMatch(mp ->
                        mp.getProject().getId().equals(projectId) &&
                                (mp.getRole() == MemberProjectRole.DEV_MANAGER || mp.getRole() == MemberProjectRole.DEV_PARTICIPANT)
                );
    }

    private boolean isAdmin(MemberRole memberRole) {
        return memberRole == MemberRole.ADMIN;
    }

    // Request(승인요청)을 작성한 멤버가 (인자의) Member인지 확인하는 메서드
    private void validateRequestWriter(Long memberId, Request request) {
        boolean isRequestWriter = request.getMember().getId().equals(memberId);
        if (!isRequestWriter) { throw new GeneralException(RequestErrorCode.USER_NOT_WRITE_REQUEST); }
    }

    // Request(승인요청)의 제목이나 내용을 수정하는 메서드
    private void updateRequestFields(RequestUpdateRequest requestUpdateRequest, Request request) {
        if(requestUpdateRequest.getTitle() != null) {
            request.updateTitle(requestUpdateRequest.getTitle());
        }
        if(requestUpdateRequest.getContent() != null) {
            request.updateContent(requestUpdateRequest.getContent());
        }
        if(requestUpdateRequest.getLinks() != null) {
            request.addLinks(linkService.buildLinks("request", request, requestUpdateRequest.getLinks()));
        }
        if(requestUpdateRequest.getMembers() != null) {
            designateApprover(requestUpdateRequest.getMembers(), request);
        }
    }

    private Request createReRequest(ReRequestCreateRequest dto, Long requestId, Member member, Stage stage) {
        Request request = buildRequest(dto.getTitle(), dto.getContent(), requestId, member, stage);
        List<RequestLink> requestLinks = linkService.buildLinks("request", request, dto.getLinks());
        request.addLinks(requestLinks);
        designateApprover(dto.getMembers(), request);
        return requestRepository.save(request);
    }

    private void designateApprover(List<MemberAssignDTO> dtos, Request request) {
        List<Long> memberIds = dtos.stream()
                .map(MemberAssignDTO::getId)
                .collect(Collectors.toList());

        List<Member> approvers = memberRepository.findAllById(memberIds);

        if (approvers.size() != memberIds.size()) {
            throw new GeneralException(MemberErrorCode.NOT_FOUND_MEMBER);
        }

        request.addApprovers(ApproverDesignation.designateApprover(request, approvers));
    }

    private Request buildRequest(String title, String content, Long parentId, Member member, Stage stage) {
        return Request.builder()
                .member(member)
                .stage(stage)
                .title(title)
                .content(content)
                .parentId(parentId==null ? null : parentId)
                .status(RequestStatus.PENDING)
                .build();
    }

    public void changeStatusToPending(Request request) {
        request.changeStatusToPending();
    }

}
