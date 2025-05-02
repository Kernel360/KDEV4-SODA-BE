package com.soda.project.application.stage.article.comment.validator;

import com.soda.global.response.GeneralException;
import com.soda.member.entity.Member;
import com.soda.member.enums.MemberRole;
import com.soda.project.domain.Project;
import com.soda.project.domain.error.ProjectErrorCode;
import com.soda.project.domain.stage.article.Article;
import com.soda.project.domain.stage.article.comment.Comment;
import com.soda.project.domain.stage.article.comment.error.CommentErrorCode;
import com.soda.project.domain.stage.article.error.ArticleErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommentValidator {

    public void validateAccessPermission(String userRole, Member member, Project project) {
        MemberRole role = MemberRole.valueOf(userRole.toUpperCase());
        if (role == MemberRole.ADMIN) {
            return;
        }

        if (isMemberInProject(project.getId(), member)) {
            return;
        }

        throw new GeneralException(ProjectErrorCode.MEMBER_NOT_IN_PROJECT);
    }

    public void validateParentCommentPresent(Comment parentComment, Article article, Long parentCommentId) {
        if (parentCommentId == null) {
            return;
        }

        if (parentComment == null) {
            throw new GeneralException(CommentErrorCode.PARENT_COMMENT_NOT_FOUND);
        }

        if (!parentComment.getArticle().getId().equals(article.getId())) {
            throw new GeneralException(ArticleErrorCode.INVALID_ARTICLE);
        }
    }

    public void validateCommentAuthor(Member member, Comment comment) {
        if (!comment.getMember().getId().equals(member.getId())) {
            throw new GeneralException(CommentErrorCode.FORBIDDEN_ACTION);
        }
    }

    private boolean isMemberInProject(Long projectId, Member member) {
        if (member.getMemberProjects() == null) {
            return false;
        }

        return member.getMemberProjects().stream()
                .filter(mp -> !mp.getIsDeleted())
                .anyMatch(mp -> mp.getProject().getId().equals(projectId));
    }
}
