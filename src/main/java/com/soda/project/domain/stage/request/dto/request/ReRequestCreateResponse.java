package com.soda.project.domain.stage.request.dto.request;

import com.soda.common.link.dto.LinkDTO;
import com.soda.project.domain.stage.request.Request;
import com.soda.project.domain.stage.request.enums.RequestStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Builder
@Getter
public class ReRequestCreateResponse {
    private Long requestId;
    private Long stageId;
    private Long memberId;
    private String memberName;
    private Long parentId;
    private String title;
    private String content;
    private List<LinkDTO> links;
    private List<ApproverDTO> approvers;
    private RequestStatus status;
    private LocalDateTime createdAt;

    public static ReRequestCreateResponse fromEntity(Request request) {
        return ReRequestCreateResponse.builder()
                .requestId(request.getId())
                .stageId(request.getStage().getId())
                .memberId(request.getMember().getId())
                .memberName(request.getMember().getName())
                .parentId(request.getParentId())
                .title(request.getTitle())
                .content(request.getContent())
                .links(
                        request.getLinks().stream()
                                .filter(link -> !link.getIsDeleted())
                                .map(LinkDTO::fromEntity)
                                .collect(Collectors.toList())
                )
                .approvers(
                        request.getApprovers().stream()
                                .map(ApproverDTO::fromEntity)
                                .collect(Collectors.toList())
                )
                .status(request.getStatus())
                .createdAt(request.getCreatedAt())
                .build();
    }
}
