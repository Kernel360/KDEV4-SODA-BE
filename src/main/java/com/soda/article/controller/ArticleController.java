package com.soda.article.controller;

import com.soda.article.dto.article.*;
import com.soda.article.service.ArticleService;
import com.soda.article.service.VoteService;
import com.soda.common.file.dto.FileDeleteResponse;
import com.soda.common.file.dto.FileUploadResponse;
import com.soda.common.file.service.FileService;
import com.soda.common.link.dto.LinkDeleteResponse;
import com.soda.common.link.service.LinkService;
import com.soda.global.response.ApiResponseForm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;
    private final FileService fileService;
    private final LinkService linkService;
    private final VoteService voteService;

    @PostMapping("/articles")
    public ResponseEntity<ApiResponseForm<ArticleCreateResponse>> createArticle(@RequestBody ArticleCreateRequest request, HttpServletRequest user) {
        Long userId = (Long) user.getAttribute("memberId");
        String userRole = (String) user.getAttribute("userRole").toString();
        ArticleCreateResponse response = articleService.createArticle(request, userId, userRole);
        return ResponseEntity.ok(ApiResponseForm.success(response, "게시글 생성 성공"));
    }

    // 전체 article 조회 & stage 별 article 조회
    @GetMapping("/projects/{projectId}/articles")
    public ResponseEntity<ApiResponseForm<List<ArticleListViewResponse>>> getAllArticles(HttpServletRequest user,
                                                                                     @PathVariable Long projectId,
                                                                                     @RequestParam(required = false) Long stageId) {
        Long userId = (Long) user.getAttribute("memberId");
        String userRole = (String) user.getAttribute("userRole").toString();
        List<ArticleListViewResponse> response = articleService.getAllArticles(userId, userRole, projectId, stageId);
        return ResponseEntity.ok(ApiResponseForm.success(response));
    }

    @GetMapping("/projects/{projectId}/articles/{articleId}")
    public ResponseEntity<ApiResponseForm<ArticleViewResponse>> getArticle(@PathVariable Long projectId, HttpServletRequest user,
                                                                           @PathVariable Long articleId) {
        Long userId = (Long) user.getAttribute("memberId");
        String userRole = (String) user.getAttribute("userRole").toString();
        ArticleViewResponse response = articleService.getArticle(projectId, userId, userRole, articleId);
        return ResponseEntity.ok(ApiResponseForm.success(response));
    }

    @DeleteMapping("/projects/{projectId}/articles/{articleId}")
    public ResponseEntity<Void> deleteArticle(@PathVariable Long projectId, HttpServletRequest user,
                                              @PathVariable Long articleId) {
        Long userId = (Long) user.getAttribute("memberId");
        String userRole = (String) user.getAttribute("userRole").toString();
        articleService.deleteArticle(projectId, userId, userRole, articleId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/articles/{articleId}")
    public ResponseEntity<ApiResponseForm<ArticleModifyResponse>> updateArticle(HttpServletRequest user,
                                                                                @PathVariable Long articleId, @RequestBody ArticleModifyRequest request) {
        Long userId = (Long) user.getAttribute("memberId");
        String userRole = (String) user.getAttribute("userRole").toString();
        ArticleModifyResponse response = articleService.updateArticle(userId, userRole, articleId, request);
        return ResponseEntity.ok(ApiResponseForm.success(response, "Article 수정 성공"));
    }

    @PostMapping("/articles/{articleId}/files")
    public ResponseEntity<ApiResponseForm<?>> uploadFiles(@PathVariable Long articleId,
                                                          @RequestPart("file") List<MultipartFile> files,
                                                          HttpServletRequest request) {
        Long memberId = (Long) request.getAttribute("memberId");
        FileUploadResponse fileUploadResponse = fileService.upload("article", articleId, memberId, files);
        return ResponseEntity.ok(ApiResponseForm.success(fileUploadResponse));
    }

    @DeleteMapping("articles/{articleId}/files/{fileId}")
    public ResponseEntity<ApiResponseForm<?>> deleteFile(@PathVariable Long fileId,
                                                         HttpServletRequest request) {
        Long memberId = (Long) request.getAttribute("memberId");
        FileDeleteResponse fileDeleteResponse = fileService.delete("article", fileId, memberId);
        return ResponseEntity.ok(ApiResponseForm.success(fileDeleteResponse));
    }

    @DeleteMapping("articles/{articleId}/links/{linkId}")
    public ResponseEntity<ApiResponseForm<?>> deleteLink(@PathVariable Long linkId,
                                                         HttpServletRequest request) {
        Long memberId = (Long) request.getAttribute("memberId");
        LinkDeleteResponse linkDeleteResponse = linkService.delete("article", linkId, memberId);
        return ResponseEntity.ok(ApiResponseForm.success(linkDeleteResponse));
    }

    @GetMapping("/articles/recent-articles")
    public ResponseEntity<ApiResponseForm<List<RecentArticleResponse>>> getRecentArticles(HttpServletRequest request) {
        Long memberId = (Long) request.getAttribute("memberId");
        List<RecentArticleResponse> recentArticles = articleService.getRecentArticlesForUser(memberId);
        return ResponseEntity.ok(ApiResponseForm.success(recentArticles));
    }

    @GetMapping("/articles/my")
    public ResponseEntity<ApiResponseForm<Page<MyArticleListResponse>>> getMyArticles(HttpServletRequest request,
                                                                                      @RequestParam(required = false) Long projectId,
                                                                                      Pageable pageable
                                                                                      ) {
        Long memberId = (Long) request.getAttribute("memberId");
        Page<MyArticleListResponse> response = articleService.getMyArticles(memberId, projectId, pageable);
        return ResponseEntity.ok(ApiResponseForm.success(response));
    }

    @PostMapping("/articles/{articleId}/vote")
    public ResponseEntity<ApiResponseForm<VoteCreateResponse>> createVote(@PathVariable Long articleId, HttpServletRequest request,
                                                                          @Valid @RequestBody VoteCreateRequest voteRequest) {
        Long userId = (Long) request.getAttribute("memberId");
        String userRole = (String) request.getAttribute("userRole").toString();
        VoteCreateResponse response = articleService.createVoteForArticle(articleId, userId, userRole, voteRequest);
        return ResponseEntity.ok(ApiResponseForm.success(response, "투표 생성 성공"));
    }

    @GetMapping("/articles/{articleId}/vote")
    public ResponseEntity<ApiResponseForm<VoteViewResponse>> getVoteInfo(@PathVariable Long articleId) {
        VoteViewResponse response = articleService.getVoteInfoForArticle(articleId);
        return ResponseEntity.ok(ApiResponseForm.success(response));
    }
}
