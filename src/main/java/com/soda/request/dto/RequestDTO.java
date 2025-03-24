package com.soda.request.dto;

import com.soda.project.domain.ProjectDTO;
import com.soda.project.entity.Project;
import com.soda.request.entity.Request;
import com.soda.request.enums.RequestStatus;
import lombok.Builder;

import java.time.LocalDateTime;

public class RequestDTO {
    private RequestStatus status;
    private String title;
    private String content;

    @Builder
    public RequestDTO(String title, String content, RequestStatus status) {
        this.title = title;
        this.content = content;
        this.status = status;
    }

    // Entity → DTO 변환
    public static RequestDTO fromEntity(Request request) {
        return RequestDTO.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .status(request.getStatus())
                .build();
    }

    // DTO → Entity 변환
    public Request toEntity() {
        return Request.builder()
                .title(this.title)
                .content(this.content)
                .status(this.status)
                .build();
    }
}
