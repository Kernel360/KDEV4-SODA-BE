package com.soda.project.interfaces.dto.article;

import com.soda.project.domain.stage.article.Vote;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class VoteCreateResponse {

    private Long voteId;
    private Long articleId;
    private String title;
    private LocalDateTime deadLine;
    private boolean allowMultipleSelection;
    private boolean allowTextAnswer;

    public static VoteCreateResponse from(Vote vote) {
        return VoteCreateResponse.builder()
                .voteId(vote.getId())
                .articleId(vote.getArticle().getId())
                .title(vote.getTitle())
                .deadLine(vote.getDeadLine())
                .allowMultipleSelection(vote.isAllowMultipleSelection())
                .allowTextAnswer(vote.isAllowTextAnswer())
                .build();
    }
}