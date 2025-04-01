package com.soda.common.link.dto;

import com.soda.common.link.error.LinkErrorCode;
import com.soda.common.link.model.LinkBase;
import com.soda.global.response.GeneralException;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class LinkUploadResponse {
    private Long domainId;
    private List<LinkDTO> links;

    public static <T extends LinkBase> LinkUploadResponse fromEntity(List<T> links) {
        if (links == null || links.isEmpty()) {
            throw new GeneralException(LinkErrorCode.FILE_LIST_EMPTY);
        }

        Long domainId = links.get(0).getDomainId();
        List<LinkDTO> linkDTOs = links.stream()
                .map(link -> LinkDTO.builder()
                        .urlAddress(link.getUrlAddress())
                        .urlDescription(link.getUrlDescription())
                        .build())
                .collect(Collectors.toList());

        return LinkUploadResponse.builder()
                .domainId(domainId)
                .links(linkDTOs)
                .build();
    }
}
