package com.soda.request.dto;

import com.soda.request.enums.RequestStatus;
import lombok.Getter;

import java.util.List;

@Getter
public class RequestApproveRequest {
    private String comment;
    private Long projectId;
    private List<ResponseLinkDTO> links;
}
