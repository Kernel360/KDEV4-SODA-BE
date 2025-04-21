package com.soda.article.service;

import com.soda.article.dto.article.*;
import com.soda.article.entity.Article;
import com.soda.article.entity.ArticleLink;
import com.soda.article.error.ArticleErrorCode;
import com.soda.article.error.VoteErrorCode;
import com.soda.article.repository.ArticleRepository;
import com.soda.common.link.service.LinkService;
import com.soda.global.log.dataLog.annotation.LoggableEntityAction;
import com.soda.global.response.GeneralException;
import com.soda.member.entity.Member;
import com.soda.member.enums.MemberRole;
import com.soda.member.service.MemberService;
import com.soda.project.entity.Project;
import com.soda.project.entity.Stage;
import com.soda.project.error.ProjectErrorCode;
import com.soda.project.service.MemberProjectService;
import com.soda.project.service.ProjectService;
import com.soda.project.service.StageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final MemberProjectService memberProjectService;
    private final StageService stageService;
    private final ProjectService projectService;
    private final ArticleFileService articleFileService;
    private final ArticleLinkService articleLinkService;
    private final MemberService memberService;
    private final LinkService linkService;
    private final VoteService voteService;

    /**
     * 게시글 생성하기
     * @param request 생성할 게시글의 상세 정보
     * @param userId 게시글을 생성하는 사용자 ID
     * @param userRole 게시글을 생성하는 사용자의 역할
     * @return 생성된 게시글의 정보
     */
    @LoggableEntityAction(action = "CREATE", entityClass = Article.class)
    @Transactional
    public ArticleCreateResponse createArticle(ArticleCreateRequest request, Long userId, String userRole) {
        Member member = memberService.findByIdAndIsDeletedFalse(userId);
        Project project = projectService.getValidProject(request.getProjectId());
        Stage stage = stageService.validateStage(request.getStageId(), project);

        checkIfMemberIsAdminOrProjectMember(userRole, member, project);

        Article parentArticle = null;
        if (request.getParentArticleId() != null) {
            parentArticle = articleRepository.findById(request.getParentArticleId())
                    .orElseThrow(() -> new GeneralException(ArticleErrorCode.PARENT_ARTICLE_NOT_FOUND));
        }

        articleLinkService.validateLinkSize(request.getLinkList());

        Article article = createArticle(request, member, stage, parentArticle);

        article = articleRepository.save(article);

        return ArticleCreateResponse.fromEntity(article);
    }

    private Article createArticle(ArticleCreateRequest request, Member member, Stage stage, Article parentArticle) {
        Article article = request.toEntity(member, stage, parentArticle);
        List<ArticleLink> links = linkService.buildLinks("article", article, request.getLinkList());
        article.addLinks(links);
        return article;
    }

    /**
     * 기존 게시글 수정
     * @param userId 수정하는 사용자 ID
     * @param userRole 수정하는 사용자의 역할
     * @param articleId 수정할 게시글의 ID
     * @param request 수정할 게시글의 새로운 정보
     * @return 수정된 게시글의 정보
     */
    @LoggableEntityAction(action = "UPDATE", entityClass = Article.class)
    @Transactional
    public ArticleModifyResponse updateArticle(Long userId, String userRole, Long articleId, ArticleModifyRequest request) {
        Member member = memberService.findByIdAndIsDeletedFalse(userId);
        Project project = projectService.getValidProject(request.getProjectId());

        checkIfMemberIsAdminOrProjectMember(userRole, member, project);

        Article article = validateArticle(articleId);

        articleLinkService.validateLinkSize(request.getLinkList());

        article.updateArticle(request.getTitle(), request.getContent(), request.getPriority(), request.getDeadLine());
        article.addLinks(linkService.buildLinks("article", article, request.getLinkList()));

        return ArticleModifyResponse.fromEntity(article);
    }

    /**
     * 게시글 삭제
     * @param projectId 게시글이 속한 프로젝트 ID
     * @param userId 삭제를 요청하는 사용자 ID
     * @param userRole 삭제를 요청하는 사용자의 역할
     * @param articleId 삭제할 게시글의 ID
     */
    @LoggableEntityAction(action = "DELETE", entityClass = Article.class)
    @Transactional
    public void deleteArticle(Long projectId, Long userId, String userRole, Long articleId) {
        Member member = memberService.findByIdAndIsDeletedFalse(userId);
        Project project = projectService.getValidProject(projectId);

        checkIfMemberIsAdminOrProjectMember(userRole, member, project);

        Article article = validateArticle(articleId);
        validateArticleNotDeleted(article);

        // 게시글 삭제
        article.delete();

        // 연관된 파일 및 링크 삭제
        articleFileService.deleteFiles(articleId, article);
        articleLinkService.deleteLinks(articleId, article);
    }

    /**
     * 게시글이 이미 삭제되었는지 검증
     * @param article 검증할 게시글
     */
    private void validateArticleNotDeleted(Article article) {
        if (article.getIsDeleted()) {
            throw new GeneralException(ArticleErrorCode.ARTICLE_ALREADY_DELETED);
        }
    }

    /**
     * 특정 프로젝트 단계에 속한 모든 게시글 조회
     * @param userId 게시글을 조회하는 사용자 ID
     * @param userRole 게시글을 조회하는 사용자의 역할
     * @param projectId 프로젝트 ID
     * @param stageId 단계 ID
     * @return 해당 조건에 맞는 게시글 리스트
     */
    public List<ArticleListViewResponse> getAllArticles(Long userId, String userRole, Long projectId, Long stageId) {
        Member member = memberService.findByIdAndIsDeletedFalse(userId);
        Project project = projectService.getValidProject(projectId);

        checkIfMemberIsAdminOrProjectMember(userRole, member, project);

        List<Article> articles = getArticlesByStageAndProject(stageId, project);

        List<ArticleListViewResponse> articleDTOList = articles.stream()
                .map(ArticleListViewResponse::fromEntity)
                .toList();

        Map<Long, List<ArticleListViewResponse>> parentToChildMap = articleDTOList.stream()
                .filter(articleDTO -> articleDTO.getParentArticleId() != null)
                .collect(Collectors.groupingBy(ArticleListViewResponse::getParentArticleId));

        List<ArticleListViewResponse> finalArticleDTOList = articleDTOList.stream()
                .map(articleDTO -> addChildArticleToParent(articleDTO, parentToChildMap))
                .toList();

        return finalArticleDTOList.stream()
                .filter(articleDTO -> articleDTO.getParentArticleId() == null)
                .collect(Collectors.toList());
    }

    /**
     * 부모 게시글에 자식 게시글을 재귀적으로 추가
     * @param articleDTO 부모 게시글
     * @param parentToChildMap 부모 게시글에 대한 자식 게시글의 맵
     * @return 자식 게시글이 추가된 부모 게시글
     */
    private ArticleListViewResponse addChildArticleToParent(ArticleListViewResponse articleDTO, Map<Long, List<ArticleListViewResponse>> parentToChildMap) {
        List<ArticleListViewResponse> childArticles = parentToChildMap.get(articleDTO.getId());

        // 답글이 있는 경우
        if (childArticles != null && !childArticles.isEmpty()) {
            articleDTO = articleDTO.withChildArticles(childArticles);

            for (ArticleListViewResponse childArticle : childArticles) {
                addChildArticleToParent(childArticle, parentToChildMap);
            }
        }

        return articleDTO;
    }

    /**
     * 특정 게시글을 조회
     * @param projectId 게시글이 속한 프로젝트 ID
     * @param userId 게시글을 조회하는 사용자 ID
     * @param userRole 게시글을 조회하는 사용자의 역할
     * @param articleId 조회할 게시글 ID
     * @return 조회된 게시글의 정보
     */
    public ArticleViewResponse getArticle(Long projectId, Long userId, String userRole, Long articleId) {
        Member member = memberService.findByIdAndIsDeletedFalse(userId);
        Project project = projectService.getValidProject(projectId);

        checkIfMemberIsAdminOrProjectMember(userRole, member, project);

        Article article = validateArticle(articleId);

        return ArticleViewResponse.fromEntity(article);
    }

    /**
     * 관리자 여부 체크 및 프로젝트 멤버 여부 체크
     * @param userRole 사용자 역할
     * @param member 사용자의 멤버 정보
     * @param project 프로젝트 정보
     * @throws GeneralException 사용자가 관리자도 아니고 프로젝트 멤버도 아닌 경우 예외 발생
     */
    private void checkIfMemberIsAdminOrProjectMember(String userRole, Member member, Project project) {
        boolean isAdmin = memberService.isAdmin(MemberRole.valueOf(userRole));
        boolean isProjectMember = memberProjectService.existsByMemberAndProjectAndIsDeletedFalse(member, project);

        if (!isAdmin && !isProjectMember) {
            throw new GeneralException(ProjectErrorCode.MEMBER_NOT_IN_PROJECT);
        }
    }

    /**
     * 단계와 프로젝트에 따른 게시글들을 조회
     * @param stageId 단계 ID
     * @param project 프로젝트 정보
     * @return 해당 단계와 프로젝트에 속한 게시글 리스트
     */
    private List<Article> getArticlesByStageAndProject(Long stageId, Project project) {
        if (stageId != null) {
            Stage stage = stageService.findById(stageId);
            return articleRepository.findByStageAndStage_Project(stage, project);
        }
        return articleRepository.findByStage_Project(project);
    }

    /**
     * 게시글을 ID로 검증
     * @param articleId 게시글 ID
     * @return 검증된 게시글
     */
    public Article validateArticle(Long articleId) {
        return articleRepository.findByIdAndIsDeletedFalse(articleId)
                .orElseThrow(() -> new GeneralException(ArticleErrorCode.INVALID_ARTICLE));
    }

    /**
     * 특정 사용자가 속한 모든 프로젝트의 Stage들에 포함된 최신 아티클 3개를 조회합니다.
     *
     * @param memberId 조회할 사용자의 ID
     * @return 최신 아티클 DTO 리스트 (최대 3개)
     */
    public List<RecentArticleResponse> getRecentArticlesForUser(Long memberId) {
        List<Long> projectIds = memberProjectService.findProjectIdsByMemberId(memberId);

        if (CollectionUtils.isEmpty(projectIds)) {
            log.info("사용자 {}는 참여중인 프로젝트가 없습니다.", memberId);
            return Collections.emptyList(); // 빈 리스트 반환
        }
        log.debug("사용자 {} 참여 프로젝트 ID 목록: {}", memberId, projectIds);

        List<Long> stageIds = stageService.findStageIdsByProjectIds(projectIds);

        if (CollectionUtils.isEmpty(stageIds)) {
            log.info("사용자 {}의 프로젝트들에 속한 Stage가 없습니다.", memberId);
            return Collections.emptyList(); // 빈 리스트 반환
        }
        log.debug("사용자 {} 관련 Stage ID 목록: {}", memberId, stageIds);

        List<Article> recentArticles = articleRepository.findTop3ByStage_IdInAndIsDeletedFalseOrderByCreatedAtDesc(stageIds);

        log.info("최신 아티클 조회 완료 (ArticleService): {}개 조회됨", recentArticles.size());

        return recentArticles.stream()
                .map(RecentArticleResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 특정 게시물에 투표를 생성
     * @param articleId 투표 생성할 게시글 ID
     * @param userId 요청 사용자 ID
     * @param userRole 요청 사용자의 역할
     * @param request 투표 생성 정보 DTO
     * @return 생성된 투표 정보 DTO
     */
    @Transactional
    public VoteCreateResponse createVoteForArticle(Long articleId, Long userId, String userRole, VoteCreateRequest request) {
        log.info("게시글 {} 에 투표 생성 요청 시작 (ArticleService): userId={}", articleId, userId);

        Member member = memberService.findByIdAndIsDeletedFalse(userId);
        Article article = validateArticle(articleId);
        Project project = article.getStage().getProject();

        if (!article.getMember().getId().equals(userId)) {
            log.warn("권한 없음: 게시글 작성자가 아닌 사용자가 투표 생성을 시도. articleId={}, userId={}", articleId, userId);
            throw new GeneralException(ArticleErrorCode.NO_PERMISSION_TO_MODIFY_ARTICLE);
        }

        // 기존에 투표가 존재하는지 확인
        if (voteService.doesActiveVoteExistForArticle(articleId)) {
            log.warn("게시글 {} 에 이미 활성 투표가 존재합니다.", articleId);
            throw new GeneralException(VoteErrorCode.VOTE_ALREADY_EXISTS);
        }

        VoteCreateResponse voteResponse = voteService.createVote(article, request);
        log.info("게시글 {} 에 투표 생성 완료 (ArticleService): voteId={}", articleId, voteResponse.getVoteId());

        return voteResponse;
    }
}
