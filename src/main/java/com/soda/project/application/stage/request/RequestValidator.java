package com.soda.project.application.stage.request;

import com.soda.global.response.GeneralException;
import com.soda.member.domain.member.MemberRole;
import com.soda.project.domain.stage.request.Request;
import com.soda.project.domain.stage.request.RequestErrorCode;
import com.soda.project.domain.stage.request.RequestStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RequestValidator {

    public void validateRequestStatus(Request parentRequest) {
        if (parentRequest.getStatus() != RequestStatus.REJECTED) {
            throw new GeneralException(RequestErrorCode.REQUEST_NOT_REJECTED);
        }
    }

    public void validaRequestWriter(Long memberId, Request request) {
        boolean isRequestWriter = request.getMember().getId().equals(memberId);
        boolean isAdmin = request.getMember().getRole() == MemberRole.ADMIN;
        if (!isRequestWriter && !isAdmin) { throw new GeneralException(RequestErrorCode.USER_NOT_WRITE_REQUEST); }
    }
}
