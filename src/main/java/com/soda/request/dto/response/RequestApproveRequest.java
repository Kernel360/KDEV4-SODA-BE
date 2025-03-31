package com.soda.request.dto.response;

import com.soda.common.link.dto.LinkDTO;
import lombok.Getter;

import java.util.List;

@Getter
public class RequestApproveRequest {
    private String comment;
    private Long projectId;
    private List<LinkDTO> links;
}
