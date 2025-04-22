package com.soda.article.repository;

import com.querydsl.core.Tuple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ArticleRepositoryCustom {

    Page<Tuple> findMyArticlesData(Long authorId, Long projectId, Pageable pageable);

}
