package com.soda.project.domain.stage.request.response.dto;

import com.soda.project.domain.stage.request.response.Response;
import com.soda.project.domain.stage.request.response.enums.ResponseStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class ResponseDeleteResponse {
    private Long responseId;
    private Long requestId;
    private Long memberId;
    private String memberName;
    private String comment;
    private ResponseStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isDeleted;

    public static ResponseDeleteResponse fromEntity(Response response) {
        return ResponseDeleteResponse.builder()
                .responseId(response.getId())
                .requestId(response.getRequest().getId())
                .memberId(response.getMember().getId())
                .memberName(response.getMember().getName())
                .comment(response.getComment())
                .status(response.getStatus())
                .createdAt(response.getCreatedAt())
                .updatedAt(response.getUpdatedAt())
                .isDeleted(response.getIsDeleted())
                .build();
    }
}