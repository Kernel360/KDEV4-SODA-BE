package com.soda.project.application.stage.request;

import com.soda.common.cacheHelper.CacheEvictionHelper;
import com.soda.global.log.data.annotation.LoggableEntityAction;
import com.soda.member.domain.member.Member;
import com.soda.member.domain.member.MemberService;
import com.soda.project.application.validator.ProjectValidator;
import com.soda.project.domain.stage.Stage;
import com.soda.project.domain.stage.StageService;
import com.soda.project.domain.stage.request.Request;
import com.soda.project.domain.stage.request.RequestService;
import com.soda.project.domain.stage.request.approver.ApproverDesignation;
import com.soda.project.interfaces.stage.request.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestFacade {
    private final RequestService requestService;
    private final MemberService memberService;
    private final StageService stageService;

    private final ProjectValidator projectValidator;
    private final RequestValidator requestValidator;

    private final CacheEvictionHelper cacheEvictionHelper;

    @LoggableEntityAction(action = "CREATE", entityClass = Request.class)
    @Transactional
    public RequestCreateResponse createRequest(Long memberId, RequestCreateRequest requestCreateRequest) {
        Member member = memberService.getMemberWithProjectOrThrow(memberId);
        Stage stage = stageService.getStageOrThrow(requestCreateRequest.getStageId());
        projectValidator.validateProjectDevAuthority(member, requestCreateRequest.getProjectId());
        requestCacheEvictForCreate(memberId, requestCreateRequest.getMembers());

        return requestService.createRequest(member, stage, requestCreateRequest);
    }

    @LoggableEntityAction(action = "CREATE", entityClass = Request.class)
    @Transactional
    public RequestCreateResponse createReRequest(Long memberId, Long requestId, ReRequestCreateRequest reRequestCreateRequest) {
        Member member = memberService.getMemberWithProjectOrThrow(memberId);
        Request parentRequest = requestService.getRequestOrThrow(requestId);
        Stage stage = stageService.getStageOrThrow(parentRequest.getStage().getId());
        projectValidator.validateProjectDevAuthority(member, stage.getProject().getId());
        requestValidator.validateRequestStatus(parentRequest);
        requestCacheEvictForCreate(memberId, reRequestCreateRequest.getMembers());

        return requestService.createReRequest(requestId, member, stage, reRequestCreateRequest);
    }

    private void requestCacheEvictForCreate(Long memberId, List<MemberAssignDTO> members) {
        cacheEvictionHelper.evictMyRequestPage0Size3(memberId);

        for (MemberAssignDTO memberAssign : members) {
            Long approverId = memberAssign.getId();
            if (approverId != null) {
                cacheEvictionHelper.evictMyRequestPage0Size3(approverId);
            }
        }
    }

    @LoggableEntityAction(action = "UPDATE", entityClass = Request.class)
    @Transactional
    public RequestUpdateResponse updateRequest(Long memberId, Long requestId, RequestUpdateRequest requestUpdateRequest) {
        Request request = requestService.getRequestOrThrow(requestId);
        requestValidator.validaRequestWriter(memberId, request);
        requestCacheEvict(memberId, request);

        return requestService.updateRequest(request, requestUpdateRequest);
    }

    @LoggableEntityAction(action = "DELETE", entityClass = Request.class)
    @Transactional
    public RequestDeleteResponse deleteRequest(Long memberId, Long requestId) {
        Request request = requestService.getRequestOrThrow(requestId);
        requestValidator.validaRequestWriter(memberId, request);
        requestCacheEvict(memberId, request);

        return requestService.deleteRequest(request);
    }

    private void requestCacheEvict(Long memberId, Request request) {
        cacheEvictionHelper.evictMyRequestPage0Size3(memberId);

        for (ApproverDesignation approver : request.getApprovers()) {
            cacheEvictionHelper.evictMyRequestPage0Size3(approver.getMember().getId());
        }
    }

    public Page<RequestDTO> findRequests(Long projectId, GetRequestCondition condition, Pageable pageable) {
        return requestService.findRequests(projectId, condition, pageable);
    }

    @Cacheable(value = "myRequests",
            key = "'member:' + #memberId + " +
                    "':page:' + #pageable.pageNumber + " +
                    "':size:' + #pageable.pageSize",
            cacheManager = "myCacheManager",
            condition = "#pageable.pageNumber == 0 and #pageable.pageSize == 3",
            unless = "#result == null or !#result.hasContent()")
    public Page<RequestDTO> findMemberRequests(Long memberId, GetMemberRequestCondition condition, Pageable pageable) {
        return requestService.findMemberRequests(memberId, condition, pageable);
    }

    public List<RequestDTO> findAllByStageId(Long stageId) {
        return requestService.findAllByStageId(stageId);
    }

    public RequestDTO findById(Long requestId) {
        return requestService.findById(requestId);
    }
}
