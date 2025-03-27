package com.soda.request.dto.response;

import com.soda.request.entity.Request;
import com.soda.request.entity.Response;
import com.soda.request.enums.RequestStatus;
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
    private RequestStatus status;
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
                .status(response.getRequest().getStatus())
                .createdAt(response.getCreatedAt())
                .updatedAt(response.getUpdatedAt())
                .isDeleted(response.getIsDeleted())
                .build();
    }
}