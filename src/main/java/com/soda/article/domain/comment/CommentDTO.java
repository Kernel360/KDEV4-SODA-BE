package com.soda.article.domain.comment;

import com.soda.article.entity.Comment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentDTO {
    private Long id; // 댓글 ID
    private String content; // 댓글 내용
    private MemberDTO member; // 댓글 작성자
    private Long articleId; // 게시글 ID
    private Long parentCommentId; // 부모 댓글 ID (대댓글의 경우)
    private List<CommentDTO> childComments; // 자식 댓글 리스트

    // 엔티티를 DTO로 변환하는 메소드
    public static CommentDTO fromEntity(Comment comment) {
        // 자식 댓글을 포함시켜서 반환
        List<CommentDTO> childCommentDTOs = comment.getChildComments() != null ?
                comment.getChildComments().stream()
                        .map(CommentDTO::fromEntity)
                        .collect(Collectors.toList()) :
                null;

        return CommentDTO.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .member(MemberDTO.builder()
                        .id(comment.getMember().getId())
                        .name(comment.getMember().getName())
                        .build())
                .articleId(comment.getArticle().getId())
                .parentCommentId(comment.getParentComment() != null ? comment.getParentComment().getId() : null)
                .childComments(childCommentDTOs) // 자식 댓글을 포함
                .build();
    }

    // 자식 댓글을 설정
    public CommentDTO withChildComments(List<CommentDTO> childComments) {
        return CommentDTO.builder()
                .id(this.id)
                .content(this.content)
                .member(this.member)
                .articleId(this.articleId)
                .parentCommentId(this.parentCommentId)
                .childComments(childComments)
                .build();
    }

    @Getter
    @Builder
    public static class MemberDTO {
        private Long id; // 회원 ID
        private String name; // 회원 이름
    }
}