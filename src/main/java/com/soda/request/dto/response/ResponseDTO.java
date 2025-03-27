package com.soda.request.dto.response;

import com.soda.article.domain.ArticleDTO;
import com.soda.request.dto.request.RequestDTO;
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
public class ResponseDTO {
    private Long responseId;
    private Long requestId;
    private Long memberId;
    private String memberName;
    private String comment;
    private List<ResponseLinkDTO> links;
    private RequestStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Entity → DTO 변환
    public static ResponseDTO fromEntity(Response response) {
        return ResponseDTO.builder()
                .responseId(response.getId())
                .requestId(response.getRequest().getId())
                .memberId(response.getMember().getId())
                .memberName(response.getMember().getName())
                .comment(response.getComment())
                .links(response.getLinks().stream()
                                .map(ResponseLinkDTO::fromEntity)
                                .collect(Collectors.toList()))
                .status(response.getRequest().getStatus())
                .createdAt(response.getCreatedAt())
                .updatedAt(response.getUpdatedAt())
                .build();
    }
}
