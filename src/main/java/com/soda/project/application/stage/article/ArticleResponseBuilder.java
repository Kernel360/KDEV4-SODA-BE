package com.soda.project.application.stage.article;

import com.querydsl.core.Tuple;
import com.soda.global.response.GeneralException;
import com.soda.project.domain.stage.article.Article;
import com.soda.project.domain.stage.article.error.ArticleErrorCode;
import com.soda.project.interfaces.dto.stage.article.ArticleListViewResponse;
import com.soda.project.interfaces.dto.stage.article.MyArticleListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ArticleResponseBuilder {

    public Page<ArticleListViewResponse> buildArticleListPageWithHierarchy(Page<Article> articlePage, Pageable pageable) {
        if (articlePage == null || articlePage.isEmpty()) {
            return Page.empty(pageable);
        }

        List<Article> articles = articlePage.getContent();

        List<ArticleListViewResponse> flatArticleDTOList = articles.stream()
                .map(ArticleListViewResponse::fromEntity)
                .toList();

        Map<Long, List<ArticleListViewResponse>> parentToChildMap = flatArticleDTOList.stream()
                .filter(dto -> dto.getParentArticleId() != null)
                .collect(Collectors.groupingBy(ArticleListViewResponse::getParentArticleId));

        List<ArticleListViewResponse> hierarchicalArticleDTOList = flatArticleDTOList.stream()
                .filter(dto -> dto.getParentArticleId() == null)
                .map(rootDto -> buildRecursiveHierarchy(rootDto, parentToChildMap))
                .collect(Collectors.toList());

        return new PageImpl<>(hierarchicalArticleDTOList, pageable, articlePage.getTotalElements());
    }

    private ArticleListViewResponse buildRecursiveHierarchy(ArticleListViewResponse currentDto, Map<Long, List<ArticleListViewResponse>> parentToChildMap) {
        List<ArticleListViewResponse> childDtos = parentToChildMap.get(currentDto.getId());

        if (childDtos != null && !childDtos.isEmpty()) {
            // 자식 DTO들이 있다면, 각 자식에 대해서도 재귀적으로 이 함수를 호출하여 그들의 자식들을 설정
            List<ArticleListViewResponse> processedChildren = childDtos.stream()
                    .map(childDto -> buildRecursiveHierarchy(childDto, parentToChildMap))
                    .collect(Collectors.toList());

            return currentDto.withChildArticles(processedChildren);
        } else {
            return currentDto;
        }
    }

    public Page<MyArticleListResponse> buildMyArticleListPage(Page<Tuple> tuplePage) {
        if (tuplePage == null || tuplePage.isEmpty()) {
            return Page.empty();
        }
        return tuplePage.map(this::mapTupleToMyArticleResponse);
    }

    private MyArticleListResponse mapTupleToMyArticleResponse(Tuple tuple) {
        // Tuple에서 데이터 추출
        Long articleId = tuple.get(0, Long.class);
        String title = tuple.get(1, String.class);
        Long projId = tuple.get(2, Long.class);
        String projName = tuple.get(3, String.class);
        Long stgId = tuple.get(4, Long.class);
        String stgName = tuple.get(5, String.class);
        LocalDateTime createdAt = tuple.get(6, LocalDateTime.class);

        // null 체크
        if (articleId == null) {
            throw new GeneralException(ArticleErrorCode.ARTICLE_DATA_CONVERSION_ERROR);
        }

        // DTO 생성
        return MyArticleListResponse.from(articleId, title, projId, projName, stgId, stgName, createdAt);
    }
}
