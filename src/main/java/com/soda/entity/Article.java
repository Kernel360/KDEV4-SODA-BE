package com.soda.entity;

import com.soda.entity.enums.ArticleStatus;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
public class Article extends BaseEntity{

    private String title;

    private String content;

    private Integer priority;

    private LocalDateTime deadline;

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

}
