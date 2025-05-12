package com.soda.project.domain.stage.article;

import com.soda.project.domain.stage.article.error.ArticleErrorCode;
import com.soda.project.domain.stage.common.file.FileStrategy;
import com.soda.global.response.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.IntStream;

@Service
@Transactional
@RequiredArgsConstructor
public class ArticleFileStrategy implements FileStrategy<Article, ArticleFile> {
    
    private final ArticleProvider articleProvider;
    private final ArticleFileProvider articleFileProvider;

    @Override
    public String getSupportedDomain() {
        return "article";
    }

    @Override
    public Article getDomainOrThrow(Long domainId) {
        return articleProvider.findById(domainId)
                .orElseThrow(() -> new GeneralException(ArticleErrorCode.INVALID_ARTICLE));
    }

    @Override
    public void validateWriter(Long memberId, Article article) {
        if (!article.getMember().getId().equals(memberId)) {
            throw new GeneralException(ArticleErrorCode.INVALID_INPUT);
        }
    }

    @Override
    public ArticleFile toEntity(String fileName, String url, Article article) {
        return ArticleFile.create(fileName, url, article);
    }

    @Override
    public List<ArticleFile> toEntities(List<String> urls, List<String> names, Article article) {
        if (urls == null || urls.isEmpty()) {return List.of();}

        return IntStream.range(0, urls.size())
                .mapToObj(i -> ArticleFile.create(urls.get(i), names.get(i), article))
                .toList();
    }

    @Override
    public void saveAll(List<ArticleFile> entities) {
        articleFileProvider.saveAll(entities);
    }

    @Override
    public ArticleFile getFileOrThrow(Long fileId) {
        return articleFileProvider.findById(fileId)
                .orElseThrow(() -> new GeneralException(ArticleErrorCode.ARTICLE_FILE_NOT_FOUND));
    }

    @Override
    public void validateFileUploader(Long memberId, ArticleFile file) {
        if (!file.getArticle().getMember().getId().equals(memberId)) {
            throw new GeneralException(ArticleErrorCode.USER_NOT_UPLOAD_ARTICLE_FILE);
        }
    }
}
