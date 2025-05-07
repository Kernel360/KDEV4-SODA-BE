package com.soda.project.application.stage.article;

import com.soda.project.domain.stage.article.Article;
import com.soda.project.interfaces.dto.stage.article.ArticleListViewResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

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
}
