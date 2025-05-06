package com.soda.project.domain.stage.article.comment;

import com.soda.common.BaseEntity;
import com.soda.member.domain.Member;
import com.soda.project.domain.stage.article.Article;
import com.soda.notification.event.CommentCreatedEvent;
import com.soda.notification.event.ReplyCreatedEvent;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
public class Comment extends BaseEntity {

    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // 부모 댓글을 위한 필드 (대댓글이 부모 댓글을 참조)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")  // 부모 댓글을 참조하는 외래키
    private Comment parentComment;

    // 자식 댓글 리스트 (양방향 관계에서 부모 댓글이 자식 댓글을 가질 수 있게 설정)
    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL)
    private List<Comment> childComments = new ArrayList<>();

    // 부모 댓글이 없으면 일반 댓글, 있으면 대댓글
    public boolean isChildComment() {
        return parentComment != null;
    }

    public Comment() {}

    @Builder
    public Comment (String content, Article article, Member member, Comment parentComment) {
        this.content = content;
        this.article = article;
        this.member = member;
        if (parentComment != null) {
            this.parentComment = parentComment;
            parentComment.addChildComment(this);
        }
    }

    protected static Comment create(String content, Member member, Article article, Comment parentComment) {
        return Comment.builder()
                .content(content)
                .article(article)
                .member(member)
                .parentComment(parentComment)
                .build();
    }

    @PostPersist
    public void publishCommentCreationEvents() {
        try {
            Long currentCommentId = this.getId();
            Article article = this.article;
            Member commenter = this.member;

            if (currentCommentId == null || article == null || commenter == null || article.getMember() == null) {
                System.out.println("@PostPersist: 필수 정보 부족 (ID, Post, Commenter, PostAuthor) for commentId=" + currentCommentId);
                return;
            }
            Long projectId = article.getStage().getProject().getId();
            Long currentPostId = article.getId();
            Member articleAuthor = article.getMember();
            String commenterNickname = commenter.getName();
            String articleTitle = article.getTitle();
            Long articleAuthorId = articleAuthor.getId();
            Long commenterId = commenter.getId();


            if (this.parentComment == null) {
                if (!commenterId.equals(articleAuthorId)) {
                    CommentCreatedEvent event = new CommentCreatedEvent(
                            this, projectId, currentCommentId, currentPostId, commenterId,
                            commenterNickname, this.content, articleTitle, articleAuthorId
                    );
                    registerEvent(event);
                }

            } else {
                Comment parent = this.parentComment;
                Member parentCommentAuthor = parent.getMember();

                if (parentCommentAuthor == null) {
                    System.out.println("@PostPersist: ParentCommentAuthor is null for replyId=" + currentCommentId);
                    return;
                }
                Long parentCommentId = parent.getId();
                Long parentCommentAuthorId = parentCommentAuthor.getId();

                ReplyCreatedEvent replyEvent = new ReplyCreatedEvent(
                        this,
                        projectId,
                        currentCommentId,
                        parentCommentId,
                        currentPostId,
                        commenterId,
                        commenterNickname,
                        this.content,
                        articleAuthorId,
                        articleTitle,
                        parentCommentAuthorId
                );
                registerEvent(replyEvent);
            }
        } catch (Exception e) {
            System.out.println("@PostPersist: Error registering event for commentId=" + this.getId() + ", Error: " + e.getMessage()); // 임시 로깅
        }
    }

    public void delete() {
        this.markAsDeleted();
    }

    public void update(String content) {
        this.content = content;
    }

    public void addChildComment(Comment childComment) {
        this.childComments.add(childComment);
    }

}
