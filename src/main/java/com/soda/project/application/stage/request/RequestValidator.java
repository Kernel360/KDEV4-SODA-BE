package com.soda.project.application.stage.request;

import com.soda.global.response.GeneralException;
import com.soda.project.domain.stage.request.Request;
import com.soda.project.domain.stage.request.enums.RequestStatus;
import com.soda.project.domain.stage.request.error.RequestErrorCode;
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
}
