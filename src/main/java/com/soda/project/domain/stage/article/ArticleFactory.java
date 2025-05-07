package com.soda.project.domain.stage.article;

import com.soda.member.domain.Member;
import com.soda.project.domain.stage.Stage;
import com.soda.project.domain.stage.article.enums.PriorityType;
import com.soda.project.domain.stage.common.link.LinkService;
import com.soda.project.interfaces.stage.common.link.dto.LinkUploadRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ArticleFactory {

    private final LinkService linkService;

    public Article createArticleWithLinks(
            String title, String content, PriorityType priority, LocalDateTime deadline,
            Member member, Stage stage, Article parentArticle,
            List<LinkUploadRequest.LinkUploadDTO> linkList) {
        Article article = Article.createArticle(title, content, priority, deadline, member, stage, parentArticle);

        if (linkList != null && !linkList.isEmpty()) {
            List<ArticleLink> articleLinks = linkService.buildLinks("article", article, linkList);
            article.addLinks(articleLinks);
        }

        return article;
    }

    public void updateLinksForArticle(Article article, List<LinkUploadRequest.LinkUploadDTO> linkList) {
        if (CollectionUtils.isEmpty(linkList)) {
            return;
        }

        // 현재 게시글의 link 주소 확인
        Set<String> existingUrls = article.getArticleLinkList().stream()
                .map(ArticleLink::getUrlAddress)
                .collect(Collectors.toSet());

        // 기존에 없는 URL 가진 데이터만 필터링
        List<LinkUploadRequest.LinkUploadDTO> newLinksToCreateData = linkList.stream()
                .filter(linkDto -> !existingUrls.contains(linkDto.getUrlAddress()))
                .toList();

        if (CollectionUtils.isEmpty(newLinksToCreateData)) {
            return;
        }

        // 새로운 URL 가진 데이터만 현재 게시글 링크에 추가
        List<ArticleLink> newArticleLinks = linkService.buildLinks("article", article, newLinksToCreateData);
        article.addLinks(newArticleLinks);
    }
}
