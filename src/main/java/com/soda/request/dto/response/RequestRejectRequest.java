package com.soda.request.dto.response;

import lombok.Getter;

import java.util.List;

@Getter
public class RequestRejectRequest {
    private String comment;
    private Long projectId;
    private List<ResponseLinkDTO> links;
}
