package com.soda.project.interfaces.stage.article.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor // Jackson 역직렬화를 위한 기본 생성자 추가
@AllArgsConstructor // Builder 패턴을 위한 모든 필드 생성자 추가
public class MyArticleListResponse {

    private Long projectId;
    private Long articleId;
    private String title;
    private String projectName;
    private Long stageId;
    private String stageName;
    private LocalDateTime createdAt;

    public static MyArticleListResponse from(Long articleId, String title, Long projectId, String projName, Long stageId, String stageName, LocalDateTime createdAt) {
        return MyArticleListResponse.builder()
                .articleId(articleId)
                .title(title)
                .projectId(projectId)
                .projectName(projName)
                .stageId(stageId)
                .stageName(stageName)
                .createdAt(createdAt)
                .build();
    }
}
