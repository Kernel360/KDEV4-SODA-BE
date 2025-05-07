package com.soda.project.domain.stage.article;

import com.soda.common.link.dto.LinkUploadRequest;
import com.soda.common.link.service.LinkService;
import com.soda.member.domain.Member;
import com.soda.project.domain.stage.Stage;
import com.soda.project.domain.stage.article.enums.PriorityType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

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

}
