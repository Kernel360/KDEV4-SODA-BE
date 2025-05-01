package com.soda.project.application.stage.request.validator;

import com.soda.global.response.GeneralException;
import com.soda.member.entity.Member;
import com.soda.project.domain.stage.request.ApproverDesignation;
import com.soda.project.domain.stage.request.error.RequestErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RequestApproverValidator {

    public void validateApprover(Member member, List<ApproverDesignation> approvers) {
        List<Member> members = approvers.stream()
                .map(ApproverDesignation::getMember)
                .collect(Collectors.toList());
        if (!members.contains(member)) {
            throw new GeneralException(RequestErrorCode.USER_IS_NOT_APPROVER);
        }
    }
}
