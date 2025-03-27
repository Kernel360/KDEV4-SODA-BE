package com.soda.request.dto.response;

import com.soda.request.entity.Response;
import com.soda.request.enums.RequestStatus;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.ResponseEntity;

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
    private RequestStatus status;
    private String comment;
    private List<ResponseLinkDTO> links;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder
    public static ResponseUpdateResponse fromEntity(Response response) {
        return ResponseUpdateResponse.builder()
                .responseId(response.getId())
                .requestId(response.getRequest().getId())
                .memberId(response.getMember().getId())
                .memberName(response.getMember().getName())
                .status(response.getRequest().getStatus())
                .comment(response.getComment())
                .links(response.getLinks().stream()
                        .map(ResponseLinkDTO::fromEntity)
                        .collect(Collectors.toList()))
                .createdAt(response.getCreatedAt())
                .updatedAt(response.getUpdatedAt())
                .build();
    }
}
