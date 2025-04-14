package com.soda.request.dto.request;

import com.soda.common.link.dto.LinkDTO;
import com.soda.request.entity.Request;
import com.soda.request.enums.RequestStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Builder
@Getter
public class RequestUpdateResponse {
    private Long requestId;
    private Long stageId;
    private Long memberId;
    private String memberName;
    private String title;
    private String content;
    private List<LinkDTO> links;
    private RequestStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static RequestUpdateResponse fromEntity(Request request) {
        return RequestUpdateResponse.builder()
                .requestId(request.getId())
                .stageId(request.getStage().getId())
                .memberId(request.getMember().getId())
                .memberName(request.getMember().getName())
                .title(request.getTitle())
                .content(request.getContent())
                .links(
                        request.getLinks().stream()
                                .map(LinkDTO::fromEntity)
                                .collect(Collectors.toList())
                )
                .status(request.getStatus())
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                .build();
    }
}
