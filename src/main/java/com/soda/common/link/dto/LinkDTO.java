package com.soda.common.link.dto;

import com.soda.common.link.model.LinkBase;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LinkDTO {
    private Long id;
    private String urlAddress;
    private String urlDescription;

    public static <T extends LinkBase> LinkDTO fromEntity(T link) {
        return LinkDTO.builder()
                .id(link.getId())
                .urlAddress(link.getUrlAddress())
                .urlDescription(link.getUrlDescription())
                .build();
    }
}
