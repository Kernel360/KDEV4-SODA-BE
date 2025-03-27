package com.soda.request.dto;

import com.soda.request.entity.ResponseLink;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResponseLinkDTO {
    private String urlAddress;
    private String urlDescription;

    public static ResponseLinkDTO fromEntity(ResponseLink link) {
        return ResponseLinkDTO.builder()
                .urlAddress(link.getUrlAddress())
                .urlDescription(link.getUrlDescription())
                .build();
    }
}
