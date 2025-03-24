package com.soda.article.domain;

import com.soda.article.entity.Article;
import com.soda.article.enums.ArticleStatus;
import com.soda.article.enums.PriorityType;
import com.soda.common.BaseEntity;
import com.soda.member.entity.Member;
import com.soda.project.entity.Stage;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ArticleDTO {
    private Long id; // 게시글 ID
    private String title; // 제목
    private String content; // 내용
    private PriorityType priority; // 우선순위 (LOW, MEDIUM, HIGH)
    private LocalDateTime deadline; // 마감일 (선택사항)
    private ArticleStatus status; // 상태 (기본값: PENDING)
    private Long memberId; // 작성자 ID (member_id)
    private Long stageId; // 프로젝트 단계 ID (stage_id)
    private Long parentArticleId; // 부모 게시글 ID (null 이면 부모 게시글이고 추가되어있으면 답글)
    private List<Long> fileList; // 첨부파일 리스트
    private List<Long> linkList; // 첨부 링크 리스트
    private List<Long> commentList; // 댓글 리스트
    private List<Long> childArticleList; // 자식 게시글 리스트

    // Entity → DTO 변환
    public static ArticleDTO fromEntity(Article article) {
        return ArticleDTO.builder()
                .id(article.getId())
                .title(article.getTitle())
                .content(article.getContent())
                .priority(article.getPriority())
                .deadline(article.getDeadline())
                .status(article.getStatus())
                .memberId(article.getMember().getId())
                .stageId(article.getStage().getId())
                .parentArticleId(article.getParentArticle().getId())
                .fileList(article.getArticleFileList().stream().map(BaseEntity::getId).toList()) // TODO file 형식으로 변경하기
                .linkList(article.getArticleLinkList().stream().map(BaseEntity::getId).toList()) // TODO link 형식으로 변경하기
                .commentList(article.getCommentList().stream().map(BaseEntity::getId).toList())
                .childArticleList(article.getChildArticles().stream().map(BaseEntity::getId).toList())
                .build();
    }

    // DTO → Entity 변환 (request)
    public Article toEntity(Member member, Stage stage) {
        return Article.builder()
                .title(this.title)
                .content(this.content)
                .priority(this.priority)
                .deadline(this.deadline)
                .status(ArticleStatus.PENDING)
                .member(member)
                .stage(stage)
                .build();
    }
}
