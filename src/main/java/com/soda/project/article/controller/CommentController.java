package com.soda.project.article.controller;

import com.soda.article.dto.comment.*;
import com.soda.project.article.dto.comment.*;
import com.soda.project.article.service.CommentService;
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
        Long userId = (Long) user.getAttribute("memberId");
        String userRole = (String) user.getAttribute("userRole").toString();
        CommentCreateResponse response = commentService.createComment(userId, userRole, request);
        return ResponseEntity.ok(ApiResponseForm.success(response, "댓글 생성 성공"));
    }

    @GetMapping("/articles/{articleId}/comments")
    public ResponseEntity<ApiResponseForm<List<CommentDTO>>> getCommentList(HttpServletRequest user, @PathVariable Long articleId) {
        Long userId = (Long) user.getAttribute("memberId");
        String userRole = (String) user.getAttribute("userRole").toString();
        List<CommentDTO> response = commentService.getCommentList(userId, userRole, articleId);
        return ResponseEntity.ok(ApiResponseForm.success(response));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(HttpServletRequest user, @PathVariable Long commentId) {
        Long userId = (Long) user.getAttribute("memberId");
        commentService.deleteComment(userId, commentId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponseForm<CommentUpdateResponse>> updateComment(HttpServletRequest user,
                                                                                @RequestBody CommentUpdateRequest request, @PathVariable Long commentId) {
        Long userId = (Long) user.getAttribute("memberId");
        CommentUpdateResponse response = commentService.updateComment(userId, request, commentId);
        return ResponseEntity.ok(ApiResponseForm.success(response, "댓글 수정 성공"));
    }
}
