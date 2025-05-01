package com.soda.project.domain.stage.request.response.dto;

import com.soda.common.link.dto.LinkDTO;
import com.soda.project.domain.stage.request.response.Response;
import com.soda.project.domain.stage.request.response.enums.ResponseStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class ResponseUpdateResponse {
    private Long responseId;
    private Long requestId;
    private Long memberId;
    private String memberName;
    private ResponseStatus status;
    private String comment;
    private List<LinkDTO> links;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder
    public static ResponseUpdateResponse fromEntity(Response response) {
        return ResponseUpdateResponse.builder()
                .responseId(response.getId())
                .requestId(response.getRequest().getId())
                .memberId(response.getMember().getId())
                .memberName(response.getMember().getName())
                .status(response.getStatus())
                .comment(response.getComment())
                .links(response.getLinks().stream()
                        .map(LinkDTO::fromEntity)
                        .collect(Collectors.toList()))
                .createdAt(response.getCreatedAt())
                .updatedAt(response.getUpdatedAt())
                .build();
    }
}
