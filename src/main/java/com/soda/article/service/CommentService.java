package com.soda.article.service;

import com.soda.article.domain.comment.*;
import com.soda.article.entity.Article;
import com.soda.article.entity.Comment;
import com.soda.article.error.ArticleErrorCode;
import com.soda.article.error.CommentErrorCode;
import com.soda.article.repository.ArticleRepository;
import com.soda.article.repository.CommentRepository;
import com.soda.global.response.GeneralException;
import com.soda.member.entity.Member;
import com.soda.member.repository.MemberRepository;
import com.soda.project.entity.Project;
import com.soda.project.error.ProjectErrorCode;
import com.soda.project.repository.MemberProjectRepository;
import com.soda.project.repository.ProjectRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;
    private final ProjectRepository projectRepository;
    private final MemberProjectRepository memberProjectRepository;
    private final ArticleRepository articleRepository;

    @Transactional
    public CommentCreateResponse createComment(Long userId, String userRole, CommentCreateRequest request) {
        // 1. 유저가 해당 프로젝트에 참여하는지 / 관리자인지 체크
        Member member = validateMember(userId);
        Project project = validateProject(request.getProjectId());
        checkMemberInProject(userRole, member, project);

        // 2. 해당 게시글이 프로젝트에 포함되어있는지 체크
        Article article = validateArticle(request.getArticleId());

        // 3. 해당 댓글이 대댓글인 경우 (아니면 null)
        Comment parentComment = null;
        if (request.getParentCommentId() != null) {
            parentComment = commentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new GeneralException(CommentErrorCode.PARENT_COMMENT_NOT_FOUND));
        }

        // 4. 댓글 생성 및 저장
        Comment comment = Comment.builder()
                .content(request.getContent())
                .article(article)
                .member(member)
                .parentComment(parentComment)
                .build();
        commentRepository.save(comment);

        return CommentCreateResponse.fromEntity(comment);
    }

    private Member validateMember(Long userId) {
        return memberRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ProjectErrorCode.MEMBER_NOT_FOUND));
    }

    private Project validateProject(Long projectId) {
        return projectRepository.findByIdAndIsDeletedFalse(projectId)
                .orElseThrow(() -> new GeneralException(ProjectErrorCode.PROJECT_NOT_FOUND));
    }

    private void checkMemberInProject(String userRole, Member member, Project project) {
        if (!isAdminOrMember(userRole, member, project)) {
            throw new GeneralException(ProjectErrorCode.MEMBER_NOT_IN_PROJECT);
        }
    }

    private boolean isAdminOrMember(String userRole, Member member, Project project) {
        if ("ADMIN".equalsIgnoreCase(userRole)) {
            return true;
        }
        return memberProjectRepository.existsByMemberAndProjectAndIsDeletedFalse(member, project);
    }

    private Article validateArticle(Long articleId) {
        return articleRepository.findByIdAndIsDeletedFalse(articleId)
                .orElseThrow(() -> new GeneralException(ArticleErrorCode.INVALID_ARTICLE));
    }


    public List<CommentDTO> getCommentList(Long userId, String userRole, Long articleId) {
        // 1. 유저의 접근 권한 확인
        Member member = validateMember(userId);
        Article article = validateArticle(articleId);

        Project project = article.getStage().getProject();
        checkMemberInProject(userRole, member, project);

        // 2. 댓글 조회
        List<Comment> comments = commentRepository.findByArticleAndIsDeletedFalse(article);

        List<CommentDTO> commentDTOList = comments.stream()
                .map(CommentDTO::fromEntity) // Comment 엔티티를 DTO로 변환
                .toList();

        // 3. 부모 댓글과 자식 댓글 관계 설정
        Map<Long, List<CommentDTO>> parentToChildMap = commentDTOList.stream()
                .filter(commentDTO -> commentDTO.getParentCommentId() != null) // 자식 댓글만 필터링
                .collect(Collectors.groupingBy(CommentDTO::getParentCommentId)); // 부모 댓글 ID로 자식 댓글 그룹화

        // 4. 자식 댓글을 부모 댓글에 설정하는 재귀적 메소드
        List<CommentDTO> finalCommentDTOList = commentDTOList.stream()
                .map(commentDTO -> addChildCommentsToParent(commentDTO, parentToChildMap)) // 각 댓글에 자식 댓글 추가
                .toList();

        // 5. 최종적으로 부모 댓글만 반환 (대댓글을 포함한 전체 트리 형태로 반환)
        return finalCommentDTOList.stream()
                .filter(commentDTO -> commentDTO.getParentCommentId() == null) // 부모 댓글만 반환
                .collect(Collectors.toList());
    }


    // 자식 댓글을 부모 댓글에 추가하는 재귀 메소드
    private CommentDTO addChildCommentsToParent(CommentDTO commentDTO, Map<Long, List<CommentDTO>> parentToChildMap) {
        // 부모 댓글에 해당하는 자식 댓글을 찾음
        List<CommentDTO> childComments = parentToChildMap.get(commentDTO.getId());

        // 자식 댓글이 있는 경우
        if (childComments != null && !childComments.isEmpty()) {
            // 자식 댓글을 부모 댓글에 설정
            commentDTO = commentDTO.withChildComments(childComments);  // 기존 댓글의 childComments 필드를 갱신

            // 자식 댓글에도 자식 댓글이 있을 수 있으므로 재귀적으로 자식 댓글을 처리
            for (CommentDTO childComment : childComments) {
                // 자식 댓글에 대해서도 재귀 호출하여 자식 댓글을 추가
                addChildCommentsToParent(childComment, parentToChildMap); // 재귀 호출
            }
        }

        return commentDTO;
    }

    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        Comment comment = getCommentAndValidateMember(userId, commentId);

        checkIfUserIsCommentAuthor(comment.getMember(), comment);

        // isDeleted = true
        comment.delete();
    }

    @Transactional
    public CommentUpdateResponse updateComment(Long userId, CommentUpdateRequest request, Long commentId) {
        Comment comment = getCommentAndValidateMember(userId, commentId);

        checkIfUserIsCommentAuthor(comment.getMember(), comment);

        comment.update(request.getContent());

        return CommentUpdateResponse.fromEntity(comment);
    }

    // 댓글 조회와 사용자 검증을 함께 처리하는 메서드
    private Comment getCommentAndValidateMember(Long userId, Long commentId) {
        // 로그인한 사용자 확인
        Member member = validateMember(userId);

        // 댓글 조회
        return commentRepository.findByIdAndIsDeletedFalse(commentId)
                .orElseThrow(() -> new GeneralException(CommentErrorCode.COMMENT_NOT_FOUND));
    }

    // 댓글 작성자와 현재 로그인한 사용자가 동일한지 확인하는 메서드
    private void checkIfUserIsCommentAuthor(Member member, Comment comment) {
        if(!comment.getMember().getId().equals(member.getId())) {
            throw new GeneralException(CommentErrorCode.FORBIDDEN_ACTION);
        }
    }
}