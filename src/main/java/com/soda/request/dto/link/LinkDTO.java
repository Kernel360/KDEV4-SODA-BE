package com.soda.request.dto.link;

import com.soda.common.LinkBase;
import com.soda.request.entity.ResponseLink;
import io.swagger.v3.oas.models.links.Link;
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
