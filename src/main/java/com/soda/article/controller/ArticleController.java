package com.soda.article.controller;

import com.soda.article.domain.ArticleDTO;
import com.soda.article.domain.ArticleModifyRequest;
import com.soda.article.service.ArticleService;
import com.soda.global.response.ApiResponseForm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @PostMapping("")
    public ResponseEntity<ApiResponseForm<ArticleDTO>> createArticle(@RequestBody ArticleModifyRequest request) {
        ArticleDTO response = articleService.createArticle(request);
        return ResponseEntity.ok(ApiResponseForm.success(response, "게시글 생성 성공"));
    }
}
