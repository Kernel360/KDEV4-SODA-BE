package com.soda.project.domain.stage.article.dto;

import com.soda.project.domain.stage.article.enums.ArticleStatus;
import com.soda.project.domain.stage.article.enums.PriorityType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ArticleSearchCondition {

    private Long stageId;

    private SearchType searchType;
    private String keyword;

    private ArticleStatus status;
    private PriorityType priorityType;

    public enum SearchType {
        TITLE_CONTENT,
        AUTHOR
    }

}
