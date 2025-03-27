package com.soda.article.service;

import com.soda.article.domain.CommentCreateRequest;
import com.soda.article.domain.CommentCreateResponse;
import com.soda.article.entity.Article;
import com.soda.article.entity.Comment;
import com.soda.article.error.ArticleErrorCode;
import com.soda.article.error.CommentErrorCode;
import com.soda.article.repository.ArticleRepository;
import com.soda.article.repository.CommentRepository;
import com.soda.global.response.GeneralException;
import com.soda.member.entity.Member;
import com.soda.member.repository.MemberRepository;
import com.soda.project.entity.Project;
import com.soda.project.error.ProjectErrorCode;
import com.soda.project.repository.MemberProjectRepository;
import com.soda.project.repository.ProjectRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;
    private final ProjectRepository projectRepository;
    private final MemberProjectRepository memberProjectRepository;
    private final ArticleRepository articleRepository;

    @Transactional
    public CommentCreateResponse createComment(HttpServletRequest user, CommentCreateRequest request) {
        // 1. 유저가 해당 프로젝트에 참여하는지 / 관리자인지 체크
        Member member = validateMember(user);
        Project project = validateProject(request.getProjectId());
        checkMemberInProject(user, member, project);

        // 2. 해당 게시글이 프로젝트에 포함되어있는지 체크
        Article article = validateArticle(request.getArticleId());

        // 3. 해당 댓글이 대댓글인 경우 (아니면 null)
        Comment parentComment = null;
        if (request.getParentCommentId() != null) {
            parentComment = commentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new GeneralException(CommentErrorCode.PARENT_COMMENT_NOT_FOUND));
        }

        // 3. 댓글 생성 및 저장
        Comment comment = Comment.builder()
                .content(request.getContent())
                .article(article)
                .member(member)
                .parentComment(parentComment)
                .build();
        commentRepository.save(comment);

        Long parentCommentId = (parentComment != null) ? parentComment.getId() : null;

        return CommentCreateResponse.builder()
                .commentId(comment.getId())
                .content(comment.getContent())
                .memberName(member.getName())
                .parentCommentId(parentCommentId)
                .build();
    }

    private Member validateMember(HttpServletRequest user) {
        Long userId = (Long) user.getAttribute("memberId");
        return memberRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ProjectErrorCode.MEMBER_NOT_FOUND));
    }

    private Project validateProject(Long projectId) {
        return projectRepository.findByIdAndIsDeletedFalse(projectId)
                .orElseThrow(() -> new GeneralException(ProjectErrorCode.PROJECT_NOT_FOUND));
    }

    private void checkMemberInProject(HttpServletRequest user, Member member, Project project) {
        String userRole = (String) user.getAttribute("userRole").toString();
        if (!isAdminOrMember(userRole, member, project)) {
            throw new GeneralException(ProjectErrorCode.MEMBER_NOT_IN_PROJECT);
        }
    }

    private boolean isAdminOrMember(String userRole, Member member, Project project) {
        if ("ADMIN".equalsIgnoreCase(userRole)) {
            return true;
        }
        return memberProjectRepository.existsByMemberAndProjectAndIsDeletedFalse(member, project);
    }

    private Article validateArticle(Long articleId) {
        return articleRepository.findByIdAndIsDeletedFalse(articleId)
                .orElseThrow(() -> new GeneralException(ArticleErrorCode.INVALID_ARTICLE));
    }
}
