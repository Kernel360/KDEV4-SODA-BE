package com.soda.request.dto;

import com.soda.request.entity.Response;
import com.soda.request.enums.RequestStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RequestRejectResponse {
    private Long responseId;
    private String comment;
    private Long requestId;
    private Long memberId;
    private String memberName;
    private RequestStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static RequestRejectResponse fromEntity(Response response) {
        return RequestRejectResponse.builder()
                .responseId(response.getId())
                .comment(response.getComment())
                .requestId(response.getId())
                .memberId(response.getMember().getId())
                .memberName(response.getMember().getName())
                .status(response.getStatus) // Response에 Status필드 추가해야함.
                .createdAt(response.getCreatedAt())
                .updatedAt(response.getUpdatedAt())
                .build();
    }
}
