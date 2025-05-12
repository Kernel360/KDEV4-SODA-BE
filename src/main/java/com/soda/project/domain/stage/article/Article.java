package com.soda.project.domain.stage.article;

import com.soda.project.domain.stage.article.comment.Comment;
import com.soda.project.domain.stage.article.enums.ArticleStatus;
import com.soda.project.domain.stage.article.enums.PriorityType;
import com.soda.common.BaseEntity;
import com.soda.member.domain.member.Member;
import com.soda.project.domain.stage.Stage;
import com.soda.project.domain.stage.article.vote.Vote;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class Article extends BaseEntity {

    private String title;

    private String content;

    @Enumerated(EnumType.STRING)
    private PriorityType priority;

    private LocalDateTime deadline;

    @Enumerated(EnumType.STRING)
    private ArticleStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stage_id", nullable = false)
    private Stage stage;

    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL)
    private List<Comment> commentList = new ArrayList<>();

    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL)
    private List<ArticleFile> articleFileList = new ArrayList<>();

    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL)
    private List<ArticleLink> articleLinkList = new ArrayList<>();

    @OneToOne(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true, optional = true)
    private Vote vote;

    // 부모 게시글을 위한 필드 (답글이 부모 게시글을 참조)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_article_id")  // 부모 댓글을 참조하는 외래키
    private Article parentArticle;

    // 자식 게시글 리스트 (양방향 관계에서 부모 게시글이 자식 게시글을 가질 수 있게 설정)
    @OneToMany(mappedBy = "parentArticle", cascade = CascadeType.ALL)
    private List<Article> childArticles = new ArrayList<>();

    // 부모 게시글이 없으면 일반 게시글, 있으면 답글
    public boolean isChildComment() {
        return parentArticle != null;
    }

    @Builder
    public Article(String title, String content, PriorityType priority, LocalDateTime deadline, Member member, Stage stage, ArticleStatus status,
                   List<ArticleFile> articleFileList, List<ArticleLink> articleLinkList, Article parentArticle) {
        this.title = title;
        this.content = content;
        this.priority = priority;
        this.deadline = deadline;
        this.member = member;
        this.stage = stage;
        this.status = status;
        this.articleFileList = articleFileList != null ? articleFileList : new ArrayList<>();
        this.articleLinkList = articleLinkList != null ? articleLinkList : new ArrayList<>();
        this.parentArticle = parentArticle;
    }

    protected static Article createArticle(String title, String content, PriorityType priority, LocalDateTime deadline,
                                           Member member, Stage stage, Article parentArticle) {
        return Article.builder()
                .title(title)
                .content(content)
                .priority(priority)
                .deadline(deadline)
                .member(member)
                .stage(stage)
                .status(ArticleStatus.PENDING)
                .parentArticle(parentArticle)
                .articleFileList(null)
                .articleLinkList(null)
                .build();
    }

    public void delete() {
        this.markAsDeleted();
        if (this.commentList != null) {
            this.commentList.forEach(Comment::delete);
        }
        if (this.articleFileList != null) {
            this.articleFileList.forEach(ArticleFile::delete);
        }
        if (this.articleLinkList != null) {
            this.articleLinkList.forEach(ArticleLink::delete);
        }
        if (this.vote != null) {
            this.vote.delete();
        }
    }

    public void updateArticle(String title, String content, PriorityType priority, LocalDateTime deadline, Stage newStage) {
        if (title != null) {
            this.title = title;
        }
        if (content != null) {
            this.content = content;
        }
        if (priority != null) {
            this.priority = priority;
        }
        this.deadline = deadline;

        if (newStage != null) {
            this.stage = newStage;
        }
    }

    public void addLinks(List<ArticleLink> links) {
        if ( this.articleLinkList == null ) {
            this.articleLinkList = new ArrayList<>();
        }
        for (ArticleLink link : links) {
            link.updateResponse(this);
            this.articleLinkList.add(link);
        }
    }

    public void associateVote(Vote vote) {
        if (vote == null) {
            // 만약 기존 vote가 있었다면 연결 해제 (orphanRemoval=true로 인해 DB에서 삭제될 수 있음)
            if (this.vote != null) {
                this.vote.disassociateArticle();
            }
            this.vote = null;
        } else {
            this.vote = vote;
            vote.associateArticle(this);
        }
    }

    public void changeStatus(ArticleStatus newStatus) {
        if (newStatus != null) {
            this.status = newStatus;
        }
    }
}
