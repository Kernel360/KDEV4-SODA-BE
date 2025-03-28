package com.soda.article.controller;

import com.soda.article.domain.comment.*;
import com.soda.article.service.CommentService;
import com.soda.global.response.ApiResponseForm;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/comments")
    public ResponseEntity<ApiResponseForm<CommentCreateResponse>> createComment(HttpServletRequest user, @RequestBody CommentCreateRequest request) {
        CommentCreateResponse response = commentService.createComment(user, request);
        return ResponseEntity.ok(ApiResponseForm.success(response, "댓글 생성 성공"));
    }

    @GetMapping("/articles/{articleId}/comments")
    public ResponseEntity<ApiResponseForm<List<CommentDTO>>> getCommentList(HttpServletRequest user, @PathVariable Long articleId) {
        List<CommentDTO> response = commentService.getCommentList(user, articleId);
        return ResponseEntity.ok(ApiResponseForm.success(response));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(HttpServletRequest user, @PathVariable Long commentId) {
        commentService.deleteComment(user, commentId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponseForm<CommentUpdateResponse>> updateComment(HttpServletRequest user,
                                                                                @RequestBody CommentUpdateRequest request, @PathVariable Long commentId) {
        CommentUpdateResponse response = commentService.updateComment(user, request, commentId);
        return ResponseEntity.ok(ApiResponseForm.success(response, "댓글 수정 성공"));
    }
}
