package com.soda.project.interfaces.stage.request.dto;

import com.soda.project.domain.stage.request.Request;
import com.soda.project.domain.stage.request.RequestStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class RequestDeleteResponse {
    private Long requestId;
    private Long stageId;
    private Long memberId;
    private String memberName;
    private Long parentId;
    private String title;
    private String content;
    private RequestStatus status;
    private LocalDateTime createdAt;
    private Boolean isDeleted;

    public static RequestDeleteResponse fromEntity(Request request) {
        return RequestDeleteResponse.builder()
                .requestId(request.getId())
                .stageId(request.getStage().getId())
                .memberId(request.getMember().getId())
                .memberName(request.getMember().getName())
                .parentId(request.getParentId() == null ? -1 : request.getParentId())
                .title(request.getTitle())
                .content(request.getContent())
                .status(request.getStatus())
                .createdAt(request.getCreatedAt())
                .isDeleted(request.getIsDeleted())
                .build();
    }
}
