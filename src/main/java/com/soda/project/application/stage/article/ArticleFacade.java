package com.soda.project.application.stage.article;

import com.soda.global.response.GeneralException;
import com.soda.member.entity.Member;
import com.soda.member.service.MemberService;
import com.soda.project.application.stage.article.vote.validator.VoteValidator;
import com.soda.project.domain.stage.article.Article;
import com.soda.project.domain.stage.article.ArticleService;
import com.soda.project.domain.stage.article.error.VoteErrorCode;
import com.soda.project.domain.stage.article.vote.VoteService;
import com.soda.project.domain.stage.article.vote.dto.VoteCreateRequest;
import com.soda.project.domain.stage.article.vote.dto.VoteCreateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArticleFacade {

    private final MemberService memberService;
    private final ArticleService articleService;
    private final VoteService voteService;

    private final VoteValidator voteValidator;

    @Transactional
    public VoteCreateResponse createVoteForArticle(Long userId, Long articleId, String userRole, VoteCreateRequest request) {
        Member member = memberService.findMemberById(userId);
        Article article = articleService.validateArticle(articleId);

        if (voteService.doesActiveVoteExistForArticle(articleId)) {
            throw new GeneralException(VoteErrorCode.VOTE_ALREADY_EXISTS);
        }

        voteValidator.validateCreateRequest(request);

        return voteService.createVoteAndItems(article, request.getTitle(), request.getAllowMultipleSelection(),
                request.getAllowTextAnswer(), request.getDeadLine(), request.getVoteItems());
    }
}
