package com.soda.request.dto;

import com.soda.request.entity.Request;
import com.soda.request.enums.RequestStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class RequestDTO {
    private Long requestId;
    private Long taskId;
    private Long memberId;
    private String memberName;
    private String title;
    private String content;
    private RequestStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Entity → DTO 변환
    public static RequestDTO fromEntity(Request request) {
        return RequestDTO.builder()
                .requestId(request.getId())
                .taskId(request.getTask().getId())
                .memberId(request.getMember().getId())
                .memberName(request.getMember().getName())
                .title(request.getTitle())
                .content(request.getContent())
                .status(request.getStatus())
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                .build();
    }

}
