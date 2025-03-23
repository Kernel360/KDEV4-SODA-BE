package com.soda.request.dto;

import lombok.Getter;

@Getter
public class RequestCreateRequest {
    private Long taskId;
    private Boolean isApproved;
}
