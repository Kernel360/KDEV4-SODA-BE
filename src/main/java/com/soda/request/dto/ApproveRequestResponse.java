package com.soda.request.dto;

import com.soda.request.entity.Request;
import com.soda.request.enums.RequestStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ApproveRequestResponse {
    private Long requestId;
    private Long taskId;
    private Long memberId;
    private String memberName;
    private String title;
    private String content;
    private RequestStatus status;
    private LocalDateTime createdAt;
    private Boolean isDeleted;

    public static ApproveRequestResponse fromEntity(Request request) {
        return ApproveRequestResponse.builder()
                .requestId(request.getId())
                .taskId(request.getTask().getId())
                .memberId(request.getMember().getId())
                .memberName(request.getMember().getName())
                .title(request.getTitle())
                .content(request.getContent())
                .status(request.getStatus())
                .createdAt(request.getCreatedAt())
                .build();
    }
}
