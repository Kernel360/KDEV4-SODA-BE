package com.soda.project.application.stage.article.validator;

import com.soda.global.response.GeneralException;
import com.soda.member.domain.Member;
import com.soda.member.domain.MemberRole;
import com.soda.project.domain.Project;
import com.soda.project.domain.ProjectErrorCode;
import com.soda.project.domain.member.MemberProjectService;
import com.soda.project.domain.stage.article.Article;
import com.soda.project.domain.stage.article.error.ArticleErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ArticleValidator {

    private final MemberProjectService memberProjectService;

    public void validateArticle(Article article, Long articleId) {
        if (article == null) {
            throw new GeneralException(ArticleErrorCode.INVALID_ARTICLE);
        }
    }

    public void validateAdminOrProjectMember(String userRoleString, Member member, Project project) {
        MemberRole userRole = null;
        if (userRoleString != null) {
            userRole = MemberRole.valueOf(userRoleString.toUpperCase());
        }

        boolean isAdmin = (userRole == MemberRole.ADMIN);
        boolean isProjectMember = memberProjectService.existsByMemberAndProjectAndIsDeletedFalse(member, project);

        if (!isAdmin && !isProjectMember) {
            throw new GeneralException(ProjectErrorCode.MEMBER_NOT_IN_PROJECT);
        }
    }

    public void validateLinkSize(List<?> linkList) {
        if (!CollectionUtils.isEmpty(linkList) && linkList.size() > 10) {
            throw new GeneralException(ArticleErrorCode.LINK_SIZE_EXCEEDED);
        }
    }

    public void validateDeletePermission(Member requester, String requesterRoleString, Article article) {
        MemberRole requesterRole = null;
        if (requesterRoleString != null) {
            requesterRole = MemberRole.valueOf(requesterRoleString.toUpperCase());
        }

        boolean isAdmin = (requesterRole == MemberRole.ADMIN);
        boolean isAuthor = article.getMember().getId().equals(requester.getId());

        if (!isAdmin && !isAuthor) {
            throw new GeneralException(ArticleErrorCode.NO_PERMISSION_TO_DELETE_ARTICLE);
        }
    }
}
