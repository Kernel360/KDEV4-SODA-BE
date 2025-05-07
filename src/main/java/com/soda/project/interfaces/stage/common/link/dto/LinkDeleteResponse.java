package com.soda.project.interfaces.stage.common.link.dto;

import com.soda.project.domain.stage.common.link.LinkBase;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LinkDeleteResponse {
    private Long id;
    private Long domainId;
    private String urlAddress;
    private String urlDescription;
    private Boolean isDeleted;

    public static <T extends LinkBase> LinkDeleteResponse fromEntity(T link) {
        return LinkDeleteResponse.builder()
                .id(link.getId())
                .domainId(link.getDomainId())
                .urlAddress(link.getUrlAddress())
                .urlDescription(link.getUrlDescription())
                .isDeleted(link.getIsDeleted())
                .build();
    }
}