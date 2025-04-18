package com.soda.article.strategy;

import com.soda.article.entity.Article;
import com.soda.article.entity.ArticleFile;
import com.soda.article.error.ArticleErrorCode;
import com.soda.article.repository.ArticleFileRepository;
import com.soda.article.repository.ArticleRepository;
import com.soda.common.file.strategy.FileStrategy;
import com.soda.global.response.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.IntStream;

@Service
@Transactional
@RequiredArgsConstructor
public class ArticleFileStrategy implements FileStrategy<Article, ArticleFile> {
    
    private final ArticleRepository articleRepository;
    private final ArticleFileRepository articleFileRepository;

    @Override
    public String getSupportedDomain() {
        return "article";
    }

    @Override
    public Article getDomainOrThrow(Long domainId) {
        return articleRepository.findById(domainId)
                .orElseThrow(() -> new GeneralException(ArticleErrorCode.INVALID_ARTICLE));
    }

    @Override
    public void validateWriter(Long memberId, Article article) {
        if (!article.getMember().getId().equals(memberId)) {
            throw new GeneralException(ArticleErrorCode.INVALID_INPUT);
        }
    }

    @Override
    public ArticleFile toEntity(MultipartFile file, String url, Article article) {
        return ArticleFile.builder()
                .name(file.getOriginalFilename())
                .url(url)
                .article(article)
                .build();
    }

    @Override
    public List<ArticleFile> toEntities(List<String> urls, List<String> names, Article domain) {
        if (urls == null || urls.isEmpty()) {
            return List.of();
        }

        return IntStream.range(0, urls.size())
                .mapToObj(i -> ArticleFile.builder()
                        .url(urls.get(i))
                        .name(names.get(i))
                        .article(domain)
                        .build())
                .toList();
    }

    @Override
    public void saveAll(List<ArticleFile> entities) {
        articleFileRepository.saveAll(entities);
    }

    @Override
    public ArticleFile getFileOrThrow(Long fileId) {
        return articleFileRepository.findById(fileId)
                .orElseThrow(() -> new GeneralException(ArticleErrorCode.ARTICLE_FILE_NOT_FOUND));
    }

    @Override
    public void validateFileUploader(Long memberId, ArticleFile file) {
        if (!file.getArticle().getMember().getId().equals(memberId)) {
            throw new GeneralException(ArticleErrorCode.USER_NOT_UPLOAD_ARTICLE_FILE);
        }
    }
}
