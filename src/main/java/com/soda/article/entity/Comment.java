package com.soda.article.entity;

import com.soda.common.BaseEntity;
import com.soda.member.entity.Member;
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
        this.parentComment = parentComment;
    }

}
