package com.soda.article.controller;

import com.soda.article.domain.CommentCreateRequest;
import com.soda.article.domain.CommentCreateResponse;
import com.soda.article.service.CommentService;
import com.soda.global.response.ApiResponseForm;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/comments")
    public ResponseEntity<ApiResponseForm<CommentCreateResponse>> createComment(HttpServletRequest user, @RequestBody CommentCreateRequest request) {
        CommentCreateResponse response = commentService.createComment(user, request);
        return ResponseEntity.ok(ApiResponseForm.success(response, "댓글 생성 성공"));
    }
}
