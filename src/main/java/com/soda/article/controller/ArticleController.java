package com.soda.article.controller;

import com.soda.article.domain.*;
import com.soda.article.service.ArticleService;
import com.soda.global.response.ApiResponseForm;
import com.soda.global.security.auth.UserDetailsImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @PostMapping("/articles")
    public ResponseEntity<ApiResponseForm<ArticleModifyResponse>> createArticle(@RequestBody ArticleModifyRequest request,
                                                                                HttpServletRequest user) {
        ArticleModifyResponse response = articleService.createArticle(request, user);
        return ResponseEntity.ok(ApiResponseForm.success(response, "게시글 생성 성공"));
    }

    // 전체 article 조회 & stage 별 article 조회
    @GetMapping("/projects/{projectId}/articles")
    public ResponseEntity<ApiResponseForm<List<ArticleViewResponse>>> getAllArticles(HttpServletRequest user,
                                                                                     @PathVariable Long projectId,
                                                                                     @RequestParam(required = false) Long stageId) {
        List<ArticleViewResponse> response = articleService.getAllArticles(user, projectId, stageId);
        return ResponseEntity.ok(ApiResponseForm.success(response));
    }

    @GetMapping("/projects/{projectId}/articles/{articleId}")
    public ResponseEntity<ApiResponseForm<ArticleViewResponse>> getArticle(@PathVariable Long projectId, HttpServletRequest user,
                                                                           @PathVariable Long articleId) {
        ArticleViewResponse response = articleService.getArticle(projectId, user, articleId);
        return ResponseEntity.ok(ApiResponseForm.success(response));
    }

    @DeleteMapping("/projects/{projectId}/articles/{articleId}")
    public ResponseEntity<Void> deleteArticle(@PathVariable Long projectId, HttpServletRequest user,
                                              @PathVariable Long articleId) {
        articleService.deleteArticle(projectId, user, articleId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/articles/{articleId}")
    public ResponseEntity<ApiResponseForm<ArticleModifyResponse>> updateArticle(HttpServletRequest user,
                                                                                @PathVariable Long articleId, @RequestBody ArticleModifyRequest request) {
        ArticleModifyResponse response = articleService.updateArticle(user, articleId, request);
        return ResponseEntity.ok(ApiResponseForm.success(response, "Article 수정 성공"));
    }

}
