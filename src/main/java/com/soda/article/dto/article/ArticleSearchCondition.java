package com.soda.article.dto.article;

import com.soda.article.enums.ArticleStatus;
import com.soda.article.enums.PriorityType;
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
