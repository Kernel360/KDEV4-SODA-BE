package com.soda.article.dto.article;

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

    public enum SearchType {
        TITLE_CONTENT,
        AUTHOR
    }

}
