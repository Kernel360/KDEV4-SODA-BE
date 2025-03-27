package com.soda.request.dto.request;

import com.soda.request.dto.link.LinkDTO;
import lombok.Getter;

import java.util.List;

@Getter
public class RequestUpdateRequest {
    private String title;
    private String content;
    private List<LinkDTO> links;
}
