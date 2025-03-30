package com.soda.request.dto.link;

import com.soda.common.link.LinkBase;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LinkDTO {
    private Long id;
    private String urlAddress;
    private String urlDescription;

    public static LinkDTO fromEntity(LinkBase link) {
        return LinkDTO.builder()
                .id(link.getId())
                .urlAddress(link.getUrlAddress())
                .urlDescription(link.getUrlDescription())
                .build();
    }
}
