package com.soda.request.dto.request;

import com.soda.common.link.dto.LinkDTO;
import lombok.Getter;

import java.util.List;

@Getter
public class RequestCreateRequest {
    private String title;
    private String content;
    private Long projectId;
    private Long stageId;
    private Long taskId;
    private List<LinkDTO> links;
}
