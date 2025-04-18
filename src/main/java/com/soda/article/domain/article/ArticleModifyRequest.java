package com.soda.article.domain.article;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.soda.article.enums.PriorityType;
import com.soda.common.link.dto.LinkUploadRequest;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ArticleModifyRequest {

    private Long projectId;
    private String title;
    private String content;
    private PriorityType priority;
    private LocalDateTime deadLine;
    private Long memberId;
    private Long stageId;
    private List<LinkUploadRequest.LinkUploadDTO> linkList;

}
