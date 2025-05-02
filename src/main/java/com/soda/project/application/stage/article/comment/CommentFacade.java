package com.soda.project.application.stage.article.comment;

import com.soda.global.log.data.annotation.LoggableEntityAction;
import com.soda.member.entity.Member;
import com.soda.member.service.MemberService;
import com.soda.project.application.stage.article.comment.builder.CommentHierarchyBuilder;
import com.soda.project.application.stage.article.comment.validator.CommentValidator;
import com.soda.project.application.stage.article.validator.ArticleValidator;
import com.soda.project.domain.Project;
import com.soda.project.domain.ProjectService;
import com.soda.project.domain.stage.article.Article;
import com.soda.project.domain.stage.article.ArticleService;
import com.soda.project.domain.stage.article.comment.Comment;
import com.soda.project.domain.stage.article.comment.CommentService;
import com.soda.project.domain.stage.article.comment.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentFacade {

    private final CommentService commentService;
    private final MemberService memberService;
    private final ArticleService articleService;
    private final ProjectService projectService;
    private final CommentHierarchyBuilder commentHierarchyBuilder;

    private final ArticleValidator articleValidator;
    private final CommentValidator commentValidator;

    @LoggableEntityAction(action = "CREATE", entityClass = Comment.class)
    @Transactional
    public CommentCreateResponse createComment(Long userId, String userRole, CommentCreateRequest request) {
        Member member = memberService.findByIdAndIsDeletedFalse(userId);
        Project project = projectService.getValidProject(request.getProjectId());
        Article article = articleService.validateArticle(request.getArticleId());

        Comment parentComment = commentService.findOptionalParentComment(request.getParentCommentId())
                .orElse(null);

        articleValidator.validateArticle(article, article.getId());
        commentValidator.validateAccessPermission(userRole, member, project);
        commentValidator.validateParentCommentPresent(parentComment, article, request.getParentCommentId());

        return commentService.createComment(request.getContent(), member, article, parentComment);
    }

    public List<CommentDTO> getCommentList(Long userId, String userRole, Long articleId) {
        Member member = memberService.findByIdAndIsDeletedFalse(userId);
        Article article = articleService.validateArticle(articleId);
        Project project = article.getStage().getProject();

        commentValidator.validateAccessPermission(userRole, member, project);

        List<Comment> comments = commentService.getCommentsForArticle(article);
        return commentHierarchyBuilder.buildHierarchy(comments);
    }

    @LoggableEntityAction(action = "DELETE", entityClass = Comment.class)
    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        Member member = memberService.findByIdAndIsDeletedFalse(userId);
        Comment comment = commentService.findCommentById(commentId);

        commentValidator.validateCommentAuthor(member, comment);

        commentService.markCommentAsDeleted(comment);
    }

    @LoggableEntityAction(action = "UPDATE", entityClass = Comment.class)
    @Transactional
    public CommentUpdateResponse updateComment(Long userId, CommentUpdateRequest request, Long commentId) {
        Member member = memberService.findByIdAndIsDeletedFalse(userId);
        Comment comment = commentService.findCommentById(commentId);

        commentValidator.validateCommentAuthor(member, comment);

        return commentService.updateCommentContent(comment, request.getContent());
    }
}
