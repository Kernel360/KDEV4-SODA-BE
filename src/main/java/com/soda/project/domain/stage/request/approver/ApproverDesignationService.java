package com.soda.project.domain.stage.request.approver;

import com.soda.global.response.GeneralException;
import com.soda.member.domain.member.MemberService;
import com.soda.project.interfaces.stage.request.dto.ApproverDeleteResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApproverDesignationService {

    private final MemberService memberService;

    private final ApproverDesignationProvider approverDesignationProvider;

    public ApproverDeleteResponse deleteApprover(ApproverDesignation approverDesignation) {
        approverDesignation.delete();
        return ApproverDeleteResponse.fromEntity(approverDesignation);
    }

    public ApproverDesignation getApproveDesignationOrThrow(Long approverId) {
        return approverDesignationProvider.findById(approverId)
                .orElseThrow(() -> new GeneralException(ApproverDesignationErrorCode.APPROVE_NOT_FOUND));
    }
}
