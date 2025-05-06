package com.soda.project.application.stage.request.approver;

import com.soda.member.domain.Member;
import com.soda.member.domain.MemberService;
import com.soda.project.domain.stage.request.approver.ApproverDesignation;
import com.soda.project.domain.stage.request.approver.ApproverDesignationService;
import com.soda.project.interfaces.stage.request.dto.ApproverDeleteResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApproverDesignationFacade {

    private final ApproverDesignationService approverDesignationService;
    private final MemberService memberService;

    private final ApproverDesignationValidator approverDesignationValidator;

    @Transactional
    public ApproverDeleteResponse deleteApprover(Long approverId, Long memberId) {
        ApproverDesignation approverDesignation = approverDesignationService.getApproveDesignationOrThrow(approverId);
        Member currentMember = memberService.findMemberById(memberId);
        approverDesignationValidator.validateRequestWriter(currentMember, approverDesignation);

        return approverDesignationService.deleteApprover(approverDesignation);
    }
}
