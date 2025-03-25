package com.soda.article.controller;

import com.soda.article.domain.ArticleDTO;
import com.soda.article.domain.ArticleModifyRequest;
import com.soda.article.domain.ArticleModifyResponse;
import com.soda.article.service.ArticleService;
import com.soda.global.response.ApiResponseForm;
import com.soda.global.security.auth.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @PostMapping("")
    public ResponseEntity<ApiResponseForm<ArticleModifyResponse>> createArticle(@RequestBody ArticleModifyRequest request,
                                                                                @AuthenticationPrincipal UserDetailsImpl userDetails
                                                                                ) {
        ArticleModifyResponse response = articleService.createArticle(request, userDetails);
        return ResponseEntity.ok(ApiResponseForm.success(response, "게시글 생성 성공"));
    }

}
