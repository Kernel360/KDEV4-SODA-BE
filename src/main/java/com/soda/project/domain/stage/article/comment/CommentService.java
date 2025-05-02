package com.soda.project.domain.stage.article.comment;

import com.soda.global.log.data.annotation.LoggableEntityAction;
import com.soda.global.response.GeneralException;
import com.soda.member.entity.Member;
import com.soda.member.enums.MemberRole;
import com.soda.member.service.MemberService;
import com.soda.project.application.stage.article.comment.builder.CommentHierarchyBuilder;
import com.soda.project.domain.Project;
import com.soda.project.domain.error.ProjectErrorCode;
import com.soda.project.domain.member.MemberProjectService;
import com.soda.project.domain.stage.article.Article;
import com.soda.project.domain.stage.article.ArticleService;
import com.soda.project.domain.stage.article.comment.dto.CommentCreateResponse;
import com.soda.project.domain.stage.article.comment.dto.CommentDTO;
import com.soda.project.domain.stage.article.comment.dto.CommentUpdateRequest;
import com.soda.project.domain.stage.article.comment.dto.CommentUpdateResponse;
import com.soda.project.domain.stage.article.comment.error.CommentErrorCode;
import com.soda.project.infrastructure.CommentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final MemberService memberService;
    private final MemberProjectService memberProjectService;
    private final ArticleService articleService;
    private final CommentProvider commentProvider;
    private final CommentHierarchyBuilder commentHierarchyBuilder;

    /**
     * 댓글 생성
     */
    @LoggableEntityAction(action = "CREATE", entityClass = Comment.class)
    @Transactional
    public CommentCreateResponse createComment(String content, Member member, Article article, Comment parentComment) {
        log.debug("CommentService: 댓글 생성 시작");
        Comment comment = Comment.create(content, member, article, parentComment);
        log.info("CommentService: 댓글 저장 완료 (via Provider): commentId={}", comment.getId());
        return CommentCreateResponse.fromEntity(commentProvider.store(comment));
    }

    /**
     * 특정 게시글에 달린 댓글 목록 조회
     */
    public List<CommentDTO> getCommentList(Article article) {
        // 1. 댓글 조회
        log.debug("CommentService: '{}' 게시글의 댓글 트리 조회 시작 (Article ID: {})", article.getTitle(), article.getId());
        List<Comment> comments = commentProvider.findAllByArticle(article);
        log.debug("CommentService: 댓글 {}건 조회 완료", comments.size());

        // 2. 트리 구조 생성 및 DTO 변환
        List<CommentDTO> commentTree = commentHierarchyBuilder.buildHierarchy(comments);

        log.info("CommentService: '{}' 게시글의 댓글 트리 조회 완료 (최상위 댓글 {}건)", article.getTitle(), commentTree.size());
        return commentTree;
    }

    /**
     * 댓글 삭제
     * @param userId 삭제하는 사용자 ID
     * @param commentId 삭제할 댓글 ID
     * @throws GeneralException 댓글을 작성한 사용자가 아닌 경우 예외 발생
     */
    @LoggableEntityAction(action = "DELETE", entityClass = Comment.class)
    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        Comment comment = getCommentAndValidateMember(userId, commentId);

        checkIfUserIsCommentAuthor(comment.getMember(), comment);

        // isDeleted = true
        comment.delete();
    }

    /**
     * 댓글 수정
     * @param userId 댓글을 수정하는 사용자 ID
     * @param request 댓글 수정 요청 정보
     * @param commentId 수정할 댓글 ID
     * @return 수정된 댓글의 정보
     * @throws GeneralException 댓글을 작성한 사용자가 아닌 경우 예외 발생
     */
    @LoggableEntityAction(action = "UPDATE", entityClass = Comment.class)
    @Transactional
    public CommentUpdateResponse updateComment(Long userId, CommentUpdateRequest request, Long commentId) {
        Comment comment = getCommentAndValidateMember(userId, commentId);

        checkIfUserIsCommentAuthor(comment.getMember(), comment);

        comment.update(request.getContent());

        return CommentUpdateResponse.fromEntity(comment);
    }

    /**
     * 댓글 조회하고, 해당 댓글을 작성한 사용자가 맞는지 확인
     * @param userId 댓글 조회하는 사용자 ID
     * @param commentId 조회할 댓글 ID
     * @return 조회된 댓글 객체
     * @throws GeneralException 댓글을 찾을 수 없거나 삭제된 댓글인 경우 예외 발생
     */
    private Comment getCommentAndValidateMember(Long userId, Long commentId) {
        // 로그인한 사용자 확인
        Member member = memberService.findByIdAndIsDeletedFalse(userId);

        // 댓글 조회
        return commentRepository.findByIdAndIsDeletedFalse(commentId)
                .orElseThrow(() -> new GeneralException(CommentErrorCode.COMMENT_NOT_FOUND));
    }

    /**
     * 현재 로그인한 사용자가 댓글 작성자인지 확인
     * @param member 로그인한 사용자 정보
     * @param comment 댓글 정보
     * @throws GeneralException 댓글 작성자가 아닌 경우 예외 발생
     */
    private void checkIfUserIsCommentAuthor(Member member, Comment comment) {
        if(!comment.getMember().getId().equals(member.getId())) {
            throw new GeneralException(CommentErrorCode.FORBIDDEN_ACTION);
        }
    }

    public Optional<Comment> findOptionalParentComment(Long parentCommentId) {
        if (parentCommentId == null) {
            return Optional.empty();
        }
        return commentRepository.findByIdAndIsDeletedFalse(parentCommentId);
    }

}