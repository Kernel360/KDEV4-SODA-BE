package com.soda.request.dto;

import com.soda.request.enums.RequestStatus;
import lombok.Getter;

@Getter
public class ApproveRequestRequest {
    private RequestStatus status;
}
