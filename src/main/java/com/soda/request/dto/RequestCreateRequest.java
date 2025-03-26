package com.soda.request.dto;

import lombok.Getter;

@Getter
public class RequestCreateRequest {
    private String title;
    private String content;
    private Long projectId;
    private Long stageId;
    private Long taskId;
}
