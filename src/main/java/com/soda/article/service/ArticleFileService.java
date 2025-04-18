package com.soda.article.service;

import com.soda.article.domain.article.ArticleFileDTO;
import com.soda.article.entity.Article;
import com.soda.article.entity.ArticleFile;
import com.soda.article.error.ArticleErrorCode;
import com.soda.article.repository.ArticleFileRepository;
import com.soda.global.response.GeneralException;
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
    private static final int MAX_SIZE = 10;

    public void processFiles(List<ArticleFileDTO> fileList, Article article) {
        if (fileList != null) {
            fileList.forEach(articleFileDTO -> {
                ArticleFile file = processFile(articleFileDTO, article);
                article.getArticleFileList().add(file);
            });
        }
    }

    public ArticleFile processFile(ArticleFileDTO fileDTO, Article article) {
        ArticleFile file = articleFileRepository.findByArticleIdAndNameAndIsDeletedTrue(article.getId(), fileDTO.getName())
                .orElse(null);

        if (file != null) {
            file.reActive();
        } else {
            file = ArticleFile.builder()
                    .name(fileDTO.getName())
                    .url(fileDTO.getUrl())
                    .article(article)
                    .build();
        }

        articleFileRepository.save(file);
        return file;
    }

    public void deleteFiles(Long articleId, Article article) {
        List<ArticleFile> existingFiles = articleFileRepository.findByArticleId(articleId);
        existingFiles.forEach(ArticleFile::delete);
        article.getArticleFileList().removeIf(existingFiles::contains);
    }

    public void validateFileSize(List<ArticleFileDTO> fileList) {
        if (fileList != null && fileList.size() > MAX_SIZE) {
            throw new GeneralException(ArticleErrorCode.INVALID_INPUT);
        }
    }
}
