package com.soda.request.dto.response;

import com.soda.request.dto.link.LinkDTO;
import com.soda.request.entity.Response;
import com.soda.request.enums.RequestStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class RequestRejectResponse {
    private Long responseId;
    private Long requestId;
    private Long memberId;
    private String memberName;
    private RequestStatus status;
    private String comment;
    private List<LinkDTO> links;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static RequestRejectResponse fromEntity(Response response) {
        return RequestRejectResponse.builder()
                .responseId(response.getId())
                .requestId(response.getRequest().getId())
                .memberId(response.getMember().getId())
                .memberName(response.getMember().getName())
                .status(response.getRequest().getStatus())
                .comment(response.getComment())
                .links(
                        response.getLinks().stream()
                                .map(LinkDTO::fromEntity)
                                .collect(Collectors.toList())
                )
                .createdAt(response.getCreatedAt())
                .updatedAt(response.getUpdatedAt())
                .build();
    }
}
