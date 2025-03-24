package com.soda.request.dto;

import lombok.Getter;

@Getter
public class RequestUpdateRequest {
    private Long requestId;
    private String title;
    private String content;
}
