package com.soda.project.domain.stage.article;

import com.soda.project.interfaces.stage.article.dto.ArticleFileDTO;
import com.soda.project.domain.stage.article.error.ArticleErrorCode;
import com.soda.project.infrastructure.stage.article.ArticleFileRepository;
import com.soda.global.response.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
