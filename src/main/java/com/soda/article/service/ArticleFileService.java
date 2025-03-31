package com.soda.article.service;

import com.soda.article.entity.ArticleFile;
import com.soda.article.repository.ArticleFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class ArticleFileService {

    private final ArticleFileRepository articleFileRepository;

    public void save(ArticleFile file) {
        articleFileRepository.save(file);
    }

    public List<ArticleFile> findByArticleId(Long articleId) {
        return articleFileRepository.findByArticleId(articleId);
    }

    public ArticleFile findByArticleIdAndNameAndIsDeletedTrue(Long id, String name) {
        return articleFileRepository.findByArticleIdAndNameAndIsDeletedTrue(id, name)
                .orElse(null);
    }
}
