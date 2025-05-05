package com.soda.project.interfaces.stage.request.dto;

import com.soda.project.domain.stage.request.approver.ApproverDesignation;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApproverDeleteResponse {
    private Long id;
    private Long requestId;
    private Long memberId;
    private Boolean isDeleted;

    public static ApproverDeleteResponse fromEntity(ApproverDesignation approver) {
        return ApproverDeleteResponse.builder()
                .id(approver.getId())
                .requestId(approver.getRequest().getId())
                .memberId(approver.getMember().getId())
                .isDeleted(approver.getIsDeleted())
                .build();
    }
}
