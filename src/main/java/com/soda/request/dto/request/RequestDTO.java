package com.soda.request.dto.request;

import com.soda.common.file.dto.FileDTO;
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
public class RequestDTO {
    private Long requestId;
    private Long stageId;
    private Long memberId;
    private String memberName;
    private String title;
    private String content;
    private List<LinkDTO> links;
    private List<FileDTO> files;
    private List<ApproverDTO> approvers;
    private RequestStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Entity → DTO 변환
    public static RequestDTO fromEntity(Request request) {
        return RequestDTO.builder()
                .requestId(request.getId())
                .stageId(request.getStage().getId())
                .memberId(request.getMember().getId())
                .memberName(request.getMember().getName())
                .title(request.getTitle())
                .content(request.getContent())
                .links(
                        request.getLinks().stream()
                                .filter(link -> !link.getIsDeleted())
                                .map(LinkDTO::fromEntity)
                                .collect(Collectors.toList())
                )
                .files(
                        request.getFiles().stream()
                                .filter(file -> !file.getIsDeleted())
                                .map(FileDTO::fromEntity)
                                .collect(Collectors.toList())
                )
                .approvers(
                        request.getApprovers().stream()
                                .map(ApproverDTO::fromEntity)
                                .collect(Collectors.toList())
                )
                .status(request.getStatus())
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                .build();
    }

}
