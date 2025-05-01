package com.soda.project.domain.stage.request.dto;

import com.soda.project.domain.stage.request.ApproverDesignation;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApproverDTO {
    private Long id;
    private Long requestId;
    private Long memberId;

    public static ApproverDTO fromEntity(ApproverDesignation entity) {
        return ApproverDTO.builder()
                .id(entity.getId())
                .requestId(entity.getRequest().getId())
                .memberId(entity.getMember().getId())
                .build();
    }
}
