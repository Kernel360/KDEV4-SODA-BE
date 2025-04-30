package com.soda.project.domain.stage.request.response.dto;

import com.soda.common.file.dto.FileDTO;
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
public class ResponseDTO {
    private Long responseId;
    private Long requestId;
    private Long memberId;
    private String memberName;
    private String comment;
    private List<LinkDTO> links;
    private List<FileDTO> files;
    private ResponseStatus status;
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
                .links(
                        response.getLinks().stream()
                                .filter(link -> !link.getIsDeleted())
                                .map(LinkDTO::fromEntity)
                                .collect(Collectors.toList())
                )
                .files(
                        response.getFiles().stream()
                                .filter(file -> !file.getIsDeleted())
                                .map(FileDTO::fromEntity)
                                .collect(Collectors.toList())
                )
                .status(response.getStatus())
                .createdAt(response.getCreatedAt())
                .updatedAt(response.getUpdatedAt())
                .build();
    }
}
