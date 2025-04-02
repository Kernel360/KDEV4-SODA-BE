package com.soda.article.service;

import com.soda.article.domain.comment.*;
import com.soda.article.entity.Article;
import com.soda.article.entity.Comment;
import com.soda.article.error.CommentErrorCode;
import com.soda.article.repository.CommentRepository;
import com.soda.global.response.GeneralException;
import com.soda.member.entity.Member;
import com.soda.member.enums.MemberRole;
import com.soda.member.error.MemberErrorCode;
import com.soda.member.service.MemberService;
import com.soda.project.entity.Project;
import com.soda.project.error.ProjectErrorCode;
import com.soda.project.service.MemberProjectService;
import com.soda.project.service.ProjectService;
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
    private final MemberService memberService;
    private final ProjectService projectService;
    private final MemberProjectService memberProjectService;
    private final ArticleService articleService;

    /**
     * 댓글 생성
     * @param userId 댓글을 작성하는 사용자 ID
     * @param userRole 댓글을 작성하는 사용자 역할
     * @param request 댓글 생성 요청 정보
     * @return 생성된 댓글의 정보
     * @throws GeneralException 사용자가 프로젝트에 참여하지 않거나 댓글을 작성할 권한이 없는 경우 예외 발생
     */
    @Transactional
    public CommentCreateResponse createComment(Long userId, String userRole, CommentCreateRequest request) {
        // 1. 유저가 해당 프로젝트에 참여하는지 / 관리자인지 체크
        Member member = memberService.findByIdAndIsDeletedFalse(userId);
        Project project = projectService.getValidProject(request.getProjectId());
        checkIfMemberIsAdmin(userRole);
        checkMemberInProject(member, project);

        // 2. 해당 게시글이 프로젝트에 포함되어있는지 체크
        Article article = articleService.validateArticle(request.getArticleId());

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

    /**
     * 관리자 여부 체크
     * @param userRole 사용자 역할
     * @throws GeneralException 사용자가 관리자 역할이 아니면 예외 발생
     */
    private void checkIfMemberIsAdmin(String userRole) {
        if (!memberService.isAdmin(MemberRole.valueOf(userRole))) {
            throw new GeneralException(MemberErrorCode.MEMBER_NOT_ADMIN);
        }
    }

    /**
     * 프로젝트에 사용자가 포함되어 있는지 확인
     * @param member 사용자 정보
     * @param project 프로젝트 정보
     * @throws GeneralException 사용자가 프로젝트의 멤버가 아닌 경우 예외 발생
     */
    private void checkMemberInProject(Member member, Project project) {
        if (!memberProjectService.existsByMemberAndProjectAndIsDeletedFalse(member, project)) {
            throw new GeneralException(ProjectErrorCode.MEMBER_NOT_IN_PROJECT);
        }
    }

    /**
     * 특정 게시글에 달린 댓글 목록 조회
     * @param userId 조회하는 사용자 ID
     * @param userRole 조회하는 사용자 역할
     * @param articleId 게시글 ID
     * @return 게시글에 달린 댓글 목록
     * @throws GeneralException 댓글을 조회할 권한이 없을 경우 예외 발생
     */
    public List<CommentDTO> getCommentList(Long userId, String userRole, Long articleId) {
        // 1. 유저의 접근 권한 확인
        Member member = memberService.findByIdAndIsDeletedFalse(userId);
        Article article = articleService.validateArticle(articleId);

        Project project = article.getStage().getProject();
        checkIfMemberIsAdmin(userRole);
        checkMemberInProject(member, project);

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


    /**
     * 자식 댓글을 부모 댓글에 추가하는 재귀 메소드
     * @param commentDTO 부모 댓글
     * @param parentToChildMap 부모 댓글에 해당하는 자식 댓글 리스트를 포함한 맵
     * @return 자식 댓글을 포함한 부모 댓글 DTO
     */
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

    /**
     * 댓글 삭제
     * @param userId 삭제하는 사용자 ID
     * @param commentId 삭제할 댓글 ID
     * @throws GeneralException 댓글을 작성한 사용자가 아닌 경우 예외 발생
     */
    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        Comment comment = getCommentAndValidateMember(userId, commentId);

        checkIfUserIsCommentAuthor(comment.getMember(), comment);

        // isDeleted = true
        comment.delete();
    }

    /**
     * 댓글 수정
     * @param userId 댓글을 수정하는 사용자 ID
     * @param request 댓글 수정 요청 정보
     * @param commentId 수정할 댓글 ID
     * @return 수정된 댓글의 정보
     * @throws GeneralException 댓글을 작성한 사용자가 아닌 경우 예외 발생
     */
    @Transactional
    public CommentUpdateResponse updateComment(Long userId, CommentUpdateRequest request, Long commentId) {
        Comment comment = getCommentAndValidateMember(userId, commentId);

        checkIfUserIsCommentAuthor(comment.getMember(), comment);

        comment.update(request.getContent());

        return CommentUpdateResponse.fromEntity(comment);
    }

    /**
     * 댓글 조회하고, 해당 댓글을 작성한 사용자가 맞는지 확인
     * @param userId 댓글 조회하는 사용자 ID
     * @param commentId 조회할 댓글 ID
     * @return 조회된 댓글 객체
     * @throws GeneralException 댓글을 찾을 수 없거나 삭제된 댓글인 경우 예외 발생
     */
    private Comment getCommentAndValidateMember(Long userId, Long commentId) {
        // 로그인한 사용자 확인
        Member member = memberService.findByIdAndIsDeletedFalse(userId);

        // 댓글 조회
        return commentRepository.findByIdAndIsDeletedFalse(commentId)
                .orElseThrow(() -> new GeneralException(CommentErrorCode.COMMENT_NOT_FOUND));
    }

    /**
     * 현재 로그인한 사용자가 댓글 작성자인지 확인
     * @param member 로그인한 사용자 정보
     * @param comment 댓글 정보
     * @throws GeneralException 댓글 작성자가 아닌 경우 예외 발생
     */
    private void checkIfUserIsCommentAuthor(Member member, Comment comment) {
        if(!comment.getMember().getId().equals(member.getId())) {
            throw new GeneralException(CommentErrorCode.FORBIDDEN_ACTION);
        }
    }

}