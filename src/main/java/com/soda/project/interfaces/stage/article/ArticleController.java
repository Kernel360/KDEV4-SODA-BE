package com.soda.project.interfaces.stage.article;

import com.soda.project.application.stage.common.FileFacade;
import com.soda.project.application.stage.common.LinkFacade;
import com.soda.project.interfaces.stage.common.link.dto.LinkDeleteResponse;
import com.soda.global.response.ApiResponseForm;
import com.soda.project.application.stage.article.ArticleFacade;
import com.soda.project.interfaces.stage.common.file.dto.*;
import com.soda.project.interfaces.stage.article.dto.*;
import com.soda.project.interfaces.stage.article.vote.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleFacade articleFacade;
    private final FileFacade fileFacade;
    private final LinkFacade linkFacade;

    @PostMapping("/articles")
    public ResponseEntity<ApiResponseForm<ArticleCreateResponse>> createArticle(@RequestBody ArticleCreateRequest request, HttpServletRequest user) {
        Long userId = (Long) user.getAttribute("memberId");
        String userRole = (String) user.getAttribute("userRole").toString();
        ArticleCreateResponse response = articleFacade.createArticle(request, userId, userRole);
        return ResponseEntity.ok(ApiResponseForm.success(response, "게시글 생성 성공"));
    }

    // 전체 article 조회 & stage 별 article 조회
    @GetMapping("/projects/{projectId}/articles")
    public ResponseEntity<ApiResponseForm<Page<ArticleListViewResponse>>> getAllArticles(HttpServletRequest user,
                                                                                         @PathVariable Long projectId,
                                                                                         @ModelAttribute ArticleSearchCondition articleSearchCondition,
                                                                                         @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Long userId = (Long) user.getAttribute("memberId");
        String userRole = (String) user.getAttribute("userRole").toString();
        Page<ArticleListViewResponse> response = articleFacade.getAllArticles(userId, userRole, projectId, articleSearchCondition, pageable);
        return ResponseEntity.ok(ApiResponseForm.success(response));
    }

    @GetMapping("/projects/{projectId}/articles/{articleId}")
    public ResponseEntity<ApiResponseForm<ArticleViewResponse>> getArticle(@PathVariable Long projectId, HttpServletRequest user,
                                                                           @PathVariable Long articleId) {
        Long userId = (Long) user.getAttribute("memberId");
        String userRole = (String) user.getAttribute("userRole").toString();
        ArticleViewResponse response = articleFacade.getArticle(projectId, userId, userRole, articleId);
        return ResponseEntity.ok(ApiResponseForm.success(response));
    }

    @DeleteMapping("/projects/{projectId}/articles/{articleId}")
    public ResponseEntity<Void> deleteArticle(@PathVariable Long projectId, HttpServletRequest user,
                                              @PathVariable Long articleId) {
        Long userId = (Long) user.getAttribute("memberId");
        String userRole = (String) user.getAttribute("userRole").toString();
        articleFacade.deleteArticle(projectId, userId, userRole, articleId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/articles/{articleId}")
    public ResponseEntity<ApiResponseForm<ArticleModifyResponse>> updateArticle(HttpServletRequest user,
                                                                                @PathVariable Long articleId, @RequestBody ArticleModifyRequest request) {
        Long userId = (Long) user.getAttribute("memberId");
        String userRole = (String) user.getAttribute("userRole").toString();
        ArticleModifyResponse response = articleFacade.updateArticle(userId, userRole, articleId, request);
        return ResponseEntity.ok(ApiResponseForm.success(response, "Article 수정 성공"));
    }

    @PostMapping("articles/{articleId}/files/presigned-urls")
    public ResponseEntity<ApiResponseForm<?>> getPresingedUrl(@PathVariable Long articleId,
                                                              @RequestBody List<FileUploadRequest> fileUploadRequests,
                                                              HttpServletRequest request) {
        Long memberId = (Long) request.getAttribute("memberId");
        PresignedUploadResponse presignedUploadResponse = fileFacade.getPresignedUrls("article", articleId, memberId, fileUploadRequests);
        return ResponseEntity.ok(ApiResponseForm.success(presignedUploadResponse));
    }

    @PostMapping("articles/{articleId}/files/confirm-upload")
    public ResponseEntity<ApiResponseForm<?>> createFileMeta(@PathVariable Long articleId,
                                                             @RequestBody List<ConfirmedFile> confirmedFiles,
                                                             HttpServletRequest request) {
        Long memberId = (Long) request.getAttribute("memberId");
        FileConfirmResponse fileConfirmResponse = fileFacade.confirmUpload("article", articleId, memberId, confirmedFiles);
        return ResponseEntity.ok(ApiResponseForm.success(fileConfirmResponse));
    }

    @DeleteMapping("articles/{articleId}/files/{fileId}")
    public ResponseEntity<ApiResponseForm<?>> deleteFile(@PathVariable Long fileId,
                                                         HttpServletRequest request) {
        Long memberId = (Long) request.getAttribute("memberId");
        FileDeleteResponse fileDeleteResponse = fileFacade.delete("article", fileId, memberId);
        return ResponseEntity.ok(ApiResponseForm.success(fileDeleteResponse));
    }

    @DeleteMapping("articles/{articleId}/links/{linkId}")
    public ResponseEntity<ApiResponseForm<?>> deleteLink(@PathVariable Long linkId,
                                                         HttpServletRequest request) {
        Long memberId = (Long) request.getAttribute("memberId");
        LinkDeleteResponse linkDeleteResponse = linkFacade.delete("article", linkId, memberId);
        return ResponseEntity.ok(ApiResponseForm.success(linkDeleteResponse));
    }

    @GetMapping("/articles/my")
    public ResponseEntity<ApiResponseForm<Page<MyArticleListResponse>>> getMyArticles(HttpServletRequest request,
                                                                                      @RequestParam(required = false) Long projectId,
                                                                                      Pageable pageable
                                                                                      ) {
        Long memberId = (Long) request.getAttribute("memberId");
        Page<MyArticleListResponse> response = articleFacade.getMyArticles(memberId, projectId, pageable);
        return ResponseEntity.ok(ApiResponseForm.success(response));
    }

    @PostMapping("/articles/{articleId}/vote")
    public ResponseEntity<ApiResponseForm<VoteCreateResponse>> createVote(@PathVariable Long articleId, HttpServletRequest request,
                                                                          @Valid @RequestBody VoteCreateRequest voteRequest) {
        Long userId = (Long) request.getAttribute("memberId");
        VoteCreateResponse response = articleFacade.createVoteForArticle(articleId, userId, voteRequest);
        return ResponseEntity.ok(ApiResponseForm.success(response, "투표 생성 성공"));
    }

    @GetMapping("/articles/{articleId}/vote")
    public ResponseEntity<ApiResponseForm<VoteViewResponse>> getVoteInfo(@PathVariable Long articleId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("memberId");
        VoteViewResponse response = articleFacade.getVoteInfoForArticle(articleId, userId);
        return ResponseEntity.ok(ApiResponseForm.success(response));
    }

    @PostMapping("/articles/{articleId}/vote/submission")
    public ResponseEntity<ApiResponseForm<VoteSubmitResponse>> submitVote(@PathVariable Long articleId, HttpServletRequest request,
                                                                          @Valid @RequestBody VoteSubmitRequest voteSubmitRequest) {
        Long userId = (Long) request.getAttribute("memberId");
        VoteSubmitResponse response = articleFacade.submitVoteForArticle(articleId, userId, voteSubmitRequest);
        return ResponseEntity.ok(ApiResponseForm.success(response, "투표하기 성공"));
    }

    @PostMapping("/articles/{articleId}/vote/items")
    public ResponseEntity<ApiResponseForm<VoteItemAddResponse>> addVoteItem(@PathVariable Long articleId, HttpServletRequest request,
                                                                            @Valid @RequestBody VoteItemAddRequest voteItemAddRequest) {
        Long userId = (Long) request.getAttribute("memberId");
        VoteItemAddResponse response = articleFacade.addVoteItem(articleId, userId, voteItemAddRequest);
        return ResponseEntity.ok(ApiResponseForm.success(response, "투표 항목 추가 성공"));
    }

    @GetMapping("/articles/{articleId}/vote-results")
    public ResponseEntity<ApiResponseForm<VoteResultResponse>> getVoteResults(@PathVariable Long articleId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("memberId");
        VoteResultResponse response = articleFacade.getVoteResults(articleId, userId);
        return ResponseEntity.ok(ApiResponseForm.success(response));
    }

    @PatchMapping("/articles/{articleId}/status")
    public ResponseEntity<ApiResponseForm<ArticleStatusUpdateResponse>> updateArticleStatus(HttpServletRequest request, @PathVariable Long articleId,
                                                                                            @Valid @RequestBody ArticleStatusUpdateRequest updateRequest) {
        Long userId = (Long) request.getAttribute("memberId");
        ArticleStatusUpdateResponse response = articleFacade.updateArticleStatus(userId, articleId, updateRequest);
        return ResponseEntity.ok(ApiResponseForm.success(response, "게시글 상태 변경 성공"));
    }
}
