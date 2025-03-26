package com.soda.request.dto;

import com.soda.request.enums.RequestStatus;
import lombok.Getter;

import java.util.List;

@Getter
public class RequestApproveRequest {
    public Long projectId;
    private List<ResponseLinkDTO> linkList;
}
