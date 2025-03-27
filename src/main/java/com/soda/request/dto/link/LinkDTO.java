package com.soda.request.dto.link;

import com.soda.request.entity.ResponseLink;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LinkDTO {
    private String urlAddress;
    private String urlDescription;

    public static LinkDTO fromEntity(ResponseLink link) {
        return LinkDTO.builder()
                .urlAddress(link.getUrlAddress())
                .urlDescription(link.getUrlDescription())
                .build();
    }
}
