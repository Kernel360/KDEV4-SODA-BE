package com.soda.project.domain.stage.request;

import com.soda.global.response.GeneralException;
import com.soda.member.entity.Member;
import com.soda.member.enums.MemberRole;
import com.soda.member.service.MemberService;
import com.soda.project.interfaces.stage.request.dto.ApproverDeleteResponse;
import com.soda.project.infrastructure.stage.request.ApproverDesignationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ApproverDesignationService {
    private final ApproverDesignationRepository approverDesignationRepository;
    private final MemberService memberService;

    @Transactional
    public ApproverDeleteResponse deleteApprover(Long approverId, Long memberId) {
        ApproverDesignation approver = getApproveDesignationOrThrow(approverId);
        Member currentMember = memberService.findMemberById(memberId);

        // 현재 유저가 승인 생성자이닞 확인
        validateRequestWriter(currentMember, approver);

        // request 소프트 삭제
        approver.delete();

        return ApproverDeleteResponse.fromEntity(approver);
    }

    private void validateRequestWriter(Member currentMember, ApproverDesignation approver) {
        if(!approver.getRequest().getMember().equals(currentMember) && !currentMember.getRole().equals(MemberRole.ADMIN)) {
            throw new GeneralException(RequestErrorCode.USER_NOT_WRITE_REQUEST);
        }
    }

    private ApproverDesignation getApproveDesignationOrThrow(Long approverId) {
        return approverDesignationRepository.findById(approverId)
                .orElseThrow(() -> new GeneralException(ApproverDesignationErrorCode.APPROVE_NOT_FOUND));
    }
}
