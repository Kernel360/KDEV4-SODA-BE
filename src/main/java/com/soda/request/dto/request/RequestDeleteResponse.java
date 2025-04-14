package com.soda.request.dto.request;

import com.soda.request.entity.Request;
import com.soda.request.enums.RequestStatus;
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
                .title(request.getTitle())
                .content(request.getContent())
                .status(request.getStatus())
                .createdAt(request.getCreatedAt())
                .isDeleted(request.getIsDeleted())
                .build();
    }
}
