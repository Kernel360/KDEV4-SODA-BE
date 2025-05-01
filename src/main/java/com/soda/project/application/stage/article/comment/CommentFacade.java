package com.soda.project.application.stage.article.comment;

import com.soda.member.entity.Member;
import com.soda.member.service.MemberService;
import com.soda.project.application.stage.article.comment.validator.CommentValidator;
import com.soda.project.application.stage.article.validator.ArticleValidator;
import com.soda.project.domain.Project;
import com.soda.project.domain.ProjectService;
import com.soda.project.domain.stage.article.Article;
import com.soda.project.domain.stage.article.ArticleService;
import com.soda.project.domain.stage.article.comment.Comment;
import com.soda.project.domain.stage.article.comment.CommentService;
import com.soda.project.domain.stage.article.comment.dto.CommentCreateRequest;
import com.soda.project.domain.stage.article.comment.dto.CommentCreateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentFacade {

    private final CommentService commentService;
    private final MemberService memberService;
    private final ArticleService articleService;
    private final ProjectService projectService;

    private final ArticleValidator articleValidator;
    private final CommentValidator commentValidator;

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
}
