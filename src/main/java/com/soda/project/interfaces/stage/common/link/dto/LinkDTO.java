package com.soda.project.interfaces.stage.common.link.dto;

import com.soda.project.domain.stage.common.link.LinkBase;
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
