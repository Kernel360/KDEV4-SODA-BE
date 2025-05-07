package com.soda.project.application.stage.article;

import com.soda.global.log.data.annotation.LoggableEntityAction;
import com.soda.global.response.GeneralException;
import com.soda.member.domain.Member;
import com.soda.member.domain.MemberService;
import com.soda.project.application.stage.article.validator.ArticleValidator;
import com.soda.project.application.stage.article.vote.validator.VoteValidator;
import com.soda.project.domain.Project;
import com.soda.project.domain.ProjectService;
import com.soda.project.domain.stage.Stage;
import com.soda.project.domain.stage.StageService;
import com.soda.project.domain.stage.article.Article;
import com.soda.project.domain.stage.article.ArticleService;
import com.soda.project.domain.stage.article.error.VoteErrorCode;
import com.soda.project.domain.stage.article.vote.*;
import com.soda.project.interfaces.dto.stage.article.*;
import com.soda.project.interfaces.dto.stage.article.vote.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArticleFacade {

    private final ProjectService projectService;
    private final StageService stageService;
    private final MemberService memberService;
    private final ArticleService articleService;
    private final VoteService voteService;
    private final VoteItemService voteItemService;

    private final ArticleValidator articleValidator;
    private final VoteValidator voteValidator;

    private final ArticleResponseBuilder articleResponseBuilder;

    @Transactional
    public VoteCreateResponse createVoteForArticle(Long articleId, Long userId, VoteCreateRequest request) {
        Member member = memberService.findMemberById(userId);
        Article article = articleService.validateArticle(articleId);
        if (voteService.doesActiveVoteExistForArticle(articleId)) {
            throw new GeneralException(VoteErrorCode.VOTE_ALREADY_EXISTS);
        }

        voteValidator.validateCreateRequest(request);

        return voteService.createVoteAndItems(article, request.getTitle(), request.getAllowMultipleSelection(),
                request.getAllowTextAnswer(), request.getDeadLine(), request.getVoteItems());
    }

    public VoteViewResponse getVoteInfoForArticle(Long articleId, Long userId) {
        Member member = memberService.findWithProjectsById(userId);
        Article article = articleService.validateArticle(articleId);

        Vote vote = article.getVote();

        if (vote == null || vote.getIsDeleted()) {
            return null;
        }
        return VoteViewResponse.from(vote);
    }

    @Transactional
    public VoteSubmitResponse submitVoteForArticle(Long articleId, Long userId, VoteSubmitRequest request) {
        Member member = memberService.findWithProjectsById(userId);
        Article article = articleService.validateArticle(articleId);
        Vote vote = article.getVote();
        if (vote == null || vote.getIsDeleted()) {
            throw new GeneralException(VoteErrorCode.VOTE_NOT_FOUND);
        }
        // 선택된 항목 엔티티 조회 (항목 선택 투표 시)
        List<VoteItem> selectedItems = Collections.emptyList();
        if (!vote.isAllowTextAnswer() && !CollectionUtils.isEmpty(request.getSelectedItemIds())) {
            // VoteItemService 통해 조회
            selectedItems = voteItemService.findVoteItemsByIds(request.getSelectedItemIds());
        }

        voteValidator.validateSubmission(vote, member, article.getMember(), article.getStage().getProject(), request);

        VoteAnswer savedAnswer = voteService.submitAnswer(vote, member, request, selectedItems);

        return VoteSubmitResponse.from(savedAnswer, request.getSelectedItemIds());
    }

    @Transactional
    public VoteItemAddResponse addVoteItem(Long articleId, Long userId, VoteItemAddRequest request) {
        Member member = memberService.findWithProjectsById(userId);
        Article article = articleService.validateArticle(articleId);
        Vote vote = article.getVote();
        if (vote == null || vote.getIsDeleted()) {
            throw new GeneralException(VoteErrorCode.VOTE_NOT_FOUND);
        }
        voteValidator.validateVoteItemAddition(vote, member, article.getMember(), article.getStage().getProject(), request.getItemText());

        VoteItem savedItem = voteItemService.createAndSaveVoteItem(vote, request.getItemText());
        vote.addVoteItem(savedItem);
        return VoteItemAddResponse.from(savedItem);
    }

    public VoteResultResponse getVoteResults(Long articleId, Long userId) {
        Member member = memberService.findWithProjectsById(userId); // 권한 체크 위해
        Article article = articleService.validateArticle(articleId);
        Vote vote = article.getVote();
        if (vote == null || vote.getIsDeleted()) {
            throw new GeneralException(VoteErrorCode.VOTE_NOT_FOUND);
        }

        voteValidator.validateResultViewPermission(member, article.getStage().getProject());

        return voteService.getVoteResultData(vote);
    }

    @LoggableEntityAction(action = "CREATE", entityClass = Article.class)
    @Transactional
    public ArticleCreateResponse createArticle(ArticleCreateRequest request, Long userId, String userRole) {
        Member member = memberService.findByIdAndIsDeletedFalse(userId);
        Project project = projectService.getValidProject(request.getProjectId());
        Stage stage = stageService.validateStage(request.getStageId(), project);

        articleValidator.validateAdminOrProjectMember(userRole, member, project);
        articleValidator.validateLinkSize(request.getLinkList());

        Article createdArticle = articleService.createArticle(
                request.getTitle(),
                request.getContent(),
                request.getPriority(),
                request.getDeadLine(),
                member,
                stage,
                request.getParentArticleId(),
                request.getLinkList()
        );
        return ArticleCreateResponse.fromEntity(createdArticle);
    }

    public Page<ArticleListViewResponse> getAllArticles(Long userId, String userRole, Long projectId, ArticleSearchCondition searchCondition, Pageable pageable) {
        Member member = memberService.findByIdAndIsDeletedFalse(userId);
        Project project = projectService.getValidProject(projectId);

        articleValidator.validateAdminOrProjectMember(userRole, member, project);

        Page<Article> articlePage = articleService.getAllArticles(projectId, searchCondition, pageable);

        return articleResponseBuilder.buildArticleListPageWithHierarchy(articlePage, pageable);
    }

    public ArticleViewResponse getArticle(Long projectId, Long userId, String userRole, Long articleId) {
        Member member = memberService.findByIdAndIsDeletedFalse(userId);
        Project project = projectService.getValidProject(projectId);
        Article article = articleService.validateArticle(articleId);

        articleValidator.validateAdminOrProjectMember(userRole, member, project);
        return articleService.getArticle(article);
    }
}
