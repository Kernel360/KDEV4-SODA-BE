package com.soda.request.dto.request;

import lombok.Getter;

@Getter
public class RequestCreateRequest {
    private String title;
    private String content;
    private Long projectId;
    private Long stageId;
    private Long taskId;
}
