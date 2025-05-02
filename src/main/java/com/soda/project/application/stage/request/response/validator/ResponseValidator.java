package com.soda.project.application.stage.request.response.validator;

import com.soda.global.response.GeneralException;
import com.soda.member.enums.MemberRole;
import com.soda.project.domain.stage.request.response.Response;
import com.soda.project.domain.stage.request.response.error.ResponseErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResponseValidator {

    public void validateResponseWriter(Response response, Long memberId) {
        boolean isRequestWriter = response.getMember().getId().equals(memberId);
        boolean isAdmin = response.getMember().getRole().equals(MemberRole.ADMIN);
        if (!isRequestWriter && !isAdmin) { throw new GeneralException(ResponseErrorCode.USER_NOT_WRITE_RESPONSE); }
    }
}
