package com.soda.request.dto.request;

import com.soda.request.entity.ApproverDesignation;
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
