package com.soda.request.dto;

import com.soda.request.entity.Request;
import com.soda.request.entity.Response;
import com.soda.request.enums.RequestStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class RequestApproveResponse {
    private Long responseId;
    private Long requestId;
    private Long memberId;
    private String memberName;
    private RequestStatus status;
    private String comment;
    private List<ResponseLinkDTO> links;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static RequestApproveResponse fromEntity(Response response) {
        return RequestApproveResponse.builder()
                .responseId(response.getId())
                .requestId(response.getRequest().getId())
                .memberId(response.getMember().getId())
                .memberName(response.getMember().getName())
                .status(response.getRequest().getStatus())
                .comment(response.getComment())
                .links(
                        response.getLinks().stream()
                                .map(ResponseLinkDTO::fromEntity)
                                .collect(Collectors.toList())
                )
                .createdAt(response.getCreatedAt())
                .updatedAt(response.getUpdatedAt())
                .build();
    }
}
