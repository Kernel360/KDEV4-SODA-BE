package com.soda.article.controller;

import com.soda.article.domain.ArticleViewResponse;
import com.soda.article.domain.ArticleModifyRequest;
import com.soda.article.domain.ArticleModifyResponse;
import com.soda.article.service.ArticleService;
import com.soda.global.response.ApiResponseForm;
import com.soda.global.security.auth.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @PostMapping("/{projectId}/articles")
    public ResponseEntity<ApiResponseForm<ArticleModifyResponse>> createArticle(@PathVariable Long projectId, @RequestBody ArticleModifyRequest request,
                                                                                @AuthenticationPrincipal UserDetailsImpl userDetails) {
        ArticleModifyResponse response = articleService.createArticle(projectId,request, userDetails);
        return ResponseEntity.ok(ApiResponseForm.success(response, "게시글 생성 성공"));
    }

    @GetMapping("/{projectId}/articles")
    public ResponseEntity<ApiResponseForm<List<ArticleViewResponse>>> getAllArticles(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                                                     @PathVariable Long projectId) {
        List<ArticleViewResponse> response = articleService.getAllArticles(userDetails, projectId);
        return ResponseEntity.ok(ApiResponseForm.success(response));
    }

    @GetMapping("/{projectId}/articles/{articleId}")
    public ResponseEntity<ApiResponseForm<ArticleViewResponse>> getArticle(@PathVariable Long projectId, @AuthenticationPrincipal UserDetailsImpl userDetails,
                                                                           @PathVariable Long articleId) {
        ArticleViewResponse response = articleService.getArticle(projectId, userDetails, articleId);
        return ResponseEntity.ok(ApiResponseForm.success(response));
    }

}
