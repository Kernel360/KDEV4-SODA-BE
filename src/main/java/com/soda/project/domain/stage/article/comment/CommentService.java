package com.soda.project.domain.stage.article.comment;

import com.soda.global.response.GeneralException;
import com.soda.member.entity.Member;
import com.soda.project.domain.stage.article.Article;
import com.soda.project.interfaces.dto.stage.article.comment.CommentCreateResponse;
import com.soda.project.interfaces.dto.stage.article.comment.CommentUpdateResponse;
import com.soda.project.domain.stage.article.comment.error.CommentErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentProvider commentProvider;

    /**
     * 댓글 생성
     */
    public CommentCreateResponse createComment(String content, Member member, Article article, Comment parentComment) {
        log.debug("CommentService: 댓글 생성 시작");
        Comment comment = Comment.create(content, member, article, parentComment);
        log.info("CommentService: 댓글 저장 완료 (via Provider): commentId={}", comment.getId());
        return CommentCreateResponse.fromEntity(commentProvider.store(comment));
    }

    /**
     * 특정 게시글에 달린 댓글 목록 조회
     */
    public List<Comment> getCommentsForArticle(Article article) {
        log.debug("CommentService: '{}' 게시글의 댓글 트리 조회 시작 (Article ID: {})", article.getTitle(), article.getId());
        List<Comment> comments = commentProvider.findAllByArticle(article);
        log.info("CommentService: '{}' 게시글 댓글 엔티티 목록 조회 완료 ({}건)", article.getTitle(), comments.size());
        return comments;
    }

    /**
     * 댓글 삭제
     */
    public void markCommentAsDeleted(Comment comment) {
        log.debug("CommentService: 댓글 삭제(상태변경) 시작 commentId={}", comment.getId());
        comment.delete();
        log.info("CommentService: 댓글 삭제(상태변경) 완료 commentId={}", comment.getId());
    }

    /**
     * 댓글 수정
     */
    public CommentUpdateResponse updateCommentContent(Comment comment, String newContent) {
        log.debug("CommentService: 댓글 수정 시작 commentId={}", comment.getId());
        comment.update(newContent);
        log.info("CommentService: 댓글 수정 완료 commentId={}", comment.getId());
        return CommentUpdateResponse.fromEntity(comment);
    }

    public Optional<Comment> findOptionalParentComment(Long parentCommentId) {
        if (parentCommentId == null) {
            return Optional.empty();
        }
        return commentProvider.findById(parentCommentId);
    }

    public Comment findCommentById(Long commentId) {
        return commentProvider.findById(commentId)
                .orElseThrow(() -> new GeneralException(CommentErrorCode.COMMENT_NOT_FOUND));
    }
}