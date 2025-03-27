package com.soda.request.dto.link;

import com.soda.common.file.LinkBase;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LinkDTO {
    private String urlAddress;
    private String urlDescription;

    public static LinkDTO fromEntity(LinkBase link) {
        return LinkDTO.builder()
                .urlAddress(link.getUrlAddress())
                .urlDescription(link.getUrlDescription())
                .build();
    }
}
