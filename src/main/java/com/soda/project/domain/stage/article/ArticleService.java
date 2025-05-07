package com.soda.project.domain.stage.article;

import com.querydsl.core.Tuple;
import com.soda.common.link.dto.LinkUploadRequest;
import com.soda.global.response.GeneralException;
import com.soda.member.domain.Member;
import com.soda.member.domain.MemberService;
import com.soda.project.domain.stage.Stage;
import com.soda.project.domain.stage.article.enums.PriorityType;
import com.soda.project.domain.stage.article.error.ArticleErrorCode;
import com.soda.project.interfaces.dto.stage.article.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleService {

    private final MemberService memberService;

    private final ArticleProvider articleProvider;
    private final ArticleFactory articleFactory;

    /**
     * 게시글 생성하기
     */
    public Article createArticle(String title, String content, PriorityType priority, LocalDateTime deadLine, Member member,
                                 Stage stage, Long parentArticleId, List<LinkUploadRequest.LinkUploadDTO> linkList) {
        log.debug("[Service] 게시글 생성 시작: title={}, memberId={}, stageId={}, parentArticleId={}",
                title, member.getId(), stage.getId(), parentArticleId != null ? parentArticleId : "없음");

        // 부모 게시글 조회
        Article parentArticle = null;
        if (parentArticleId != null) {
            parentArticle = getValidArticleOrNull(parentArticleId);
        }

        Article article = articleFactory.createArticleWithLinks(
                title, content, priority, deadLine,
                member, stage, parentArticle,
                linkList
        );

        Article savedArticle = articleProvider.store(article);
        log.info("[Service] 게시글 저장 완료: articleId={}", savedArticle.getId());
        return savedArticle;
    }

    private Article getValidArticleOrNull(Long articleId) {
        if (articleId == null) {
            return null;
        }

        return articleProvider.findById(articleId)
                .orElseThrow(() -> {
                    log.warn("[Service] (부모) 게시글을 찾을 수 없음: articleId={}", articleId);
                    return new GeneralException(ArticleErrorCode.PARENT_ARTICLE_NOT_FOUND);
                });
    }

    /**
     * 기존 게시글 수정
     */
    public Article updateArticle(Article article, String title, String content, PriorityType priority, LocalDateTime deadline,
                                 List<LinkUploadRequest.LinkUploadDTO> linkList) {
        article.updateArticle(title, content, priority, deadline);
        articleFactory.updateLinksForArticle(article, linkList);
        return article;
    }

    /**
     * 게시글 삭제
     */
    @Transactional
    public void deleteArticle(Article article) {
        article.delete();
    }

    /**
     * 특정 프로젝트 단계에 속한 모든 게시글 조회
     */
    public Page<Article> getAllArticles(Long projectId, ArticleSearchCondition articleSearchCondition, Pageable pageable) {
        return articleProvider.searchArticles(projectId, articleSearchCondition, pageable);
    }

    /**
     * 특정 게시글을 조회
     */
    public ArticleViewResponse getArticle(Article article) {
        return ArticleViewResponse.fromEntity(article);
    }

    /**
     * 게시글을 ID로 검증
     */
    public Article validateArticle(Long articleId) {
        return articleProvider.findByIdAndIsDeletedFalseWithMemberAndCompanyUsingQuerydsl(articleId)
                .orElseThrow(() -> {
                    log.warn("유효하지 않은 게시물입니다");
                    return new GeneralException(ArticleErrorCode.INVALID_ARTICLE);
                });
    }

    /**
     * 사용자가 작성한 게시글 목록 조회
     */
    public Page<Tuple> findMyArticlesData(Long userId, Long projectId, Pageable pageable) {
        return articleProvider.findMyArticlesData(userId, projectId, pageable);
    }

    @Transactional
    public ArticleStatusUpdateResponse updateArticleStatus(Long userId, Long articleId, ArticleStatusUpdateRequest request) {
        log.info("게시글 상태 변경 시작: articleId={}, userId={}, newStatus={}",
                articleId, userId, request.getStatus());

        Article article = validateArticle(articleId);
        Member member = memberService.findMemberById(userId);

        if (member == null || !member.getId().equals(userId)) {
            log.warn("게시글 상태 변경 권한 없음: 요청자(ID:{})가 작성자(ID:{})가 아닙니다. Article ID: {}",
                    userId, (member != null ? member.getId() : "null"), articleId);
            throw new GeneralException(ArticleErrorCode.NO_PERMISSION_TO_MODIFY_ARTICLE);
        }
        log.debug("게시글 작성자 본인 확인 완료. User ID: {}", userId);

        article.changeStatus(request.getStatus());
        articleProvider.store(article);
        log.info("게시글 상태 변경 완료: articleID={}, newStatus={}", article.getId(), article.getStatus());

        return ArticleStatusUpdateResponse.from(article);
    }


}
