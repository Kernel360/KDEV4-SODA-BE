package com.soda.request.dto.response;

import lombok.Getter;

import java.util.List;

@Getter
public class ResponseUpdateRequest {
    private String comment;
    private Long projectId;
}