package com.soda.project.application.stage.request.approver;

import com.soda.global.response.GeneralException;
import com.soda.member.domain.member.Member;
import com.soda.member.domain.member.MemberRole;
import com.soda.project.domain.stage.request.approver.ApproverDesignation;
import com.soda.project.domain.stage.request.RequestErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApproverDesignationValidator {
    public void validateRequestWriter(Member currentMember, ApproverDesignation approver) {
        if(!approver.getRequest().getMember().equals(currentMember) && !currentMember.getRole().equals(MemberRole.ADMIN)) {
            throw new GeneralException(RequestErrorCode.USER_NOT_WRITE_REQUEST);
        }
    }
}
