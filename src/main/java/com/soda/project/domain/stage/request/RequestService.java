package com.soda.project.domain.stage.request;

import com.soda.common.file.service.FileService;
import com.soda.common.file.service.S3Service;
import com.soda.common.link.service.LinkService;
import com.soda.global.log.data.annotation.LoggableEntityAction;
import com.soda.global.response.GeneralException;
import com.soda.member.entity.Member;
import com.soda.member.error.MemberErrorCode;
import com.soda.member.repository.MemberRepository;
import com.soda.project.domain.stage.Stage;
import com.soda.project.domain.stage.request.dto.*;
import com.soda.project.domain.stage.request.error.RequestErrorCode;
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


    public RequestCreateResponse createRequest(Member member, Stage stage, RequestCreateRequest requestCreateRequest) {
        Request request = requestFactory.createRequest(member, stage, requestCreateRequest);
        return RequestCreateResponse.fromEntity(requestProvider.store(request));
    }

    public RequestCreateResponse createReRequest(Long requestId, Member member, Stage stage, ReRequestCreateRequest reRequestCreateRequest) {
        Request reRequest = requestFactory.createReRequest(requestId, member, stage, reRequestCreateRequest);
        return RequestCreateResponse.fromEntity(requestProvider.store(reRequest));
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


    public Request getRequestOrThrow(Long requestId) {
        return requestProvider.findById(requestId).orElseThrow(() -> new GeneralException(RequestErrorCode.REQUEST_NOT_FOUND));
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

    public void changeStatusToPending(Request request) {
        request.changeStatusToPending();
    }

}
