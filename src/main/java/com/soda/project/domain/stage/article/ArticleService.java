package com.soda.project.domain.stage.article;

import com.querydsl.core.Tuple;
import com.soda.common.link.dto.LinkUploadRequest;
import com.soda.common.link.service.LinkService;
import com.soda.global.log.data.annotation.LoggableEntityAction;
import com.soda.global.response.GeneralException;
import com.soda.member.domain.Member;
import com.soda.member.domain.MemberRole;
import com.soda.member.domain.MemberService;
import com.soda.project.application.stage.article.ArticleFacade;
import com.soda.project.domain.Project;
import com.soda.project.domain.ProjectErrorCode;
import com.soda.project.domain.ProjectService;
import com.soda.project.domain.member.MemberProjectService;
import com.soda.project.domain.stage.Stage;
import com.soda.project.domain.stage.StageService;
import com.soda.project.domain.stage.article.enums.ArticleStatus;
import com.soda.project.domain.stage.article.enums.PriorityType;
import com.soda.project.domain.stage.article.error.ArticleErrorCode;
import com.soda.project.interfaces.dto.stage.article.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class ArticleService {

    private final MemberProjectService memberProjectService;
    private final StageService stageService;
    private final ProjectService projectService;
    private final ArticleFileService articleFileService;
    private final ArticleLinkService articleLinkService;
    private final MemberService memberService;
    private final LinkService linkService;

    private final ArticleProvider articleProvider;
    private final ArticleFactory articleFactory;

    /**
     * 게시글 생성하기
     */
    public Article createArticle(String title, String content, PriorityType priority, LocalDateTime deadLine, Member member,
                                 Stage stage, Long parentArticleId, List<LinkUploadRequest.LinkUploadDTO> linkList) {
        log.debug("[Service] 게시글 생성 시작: title={}, memberId={}, stageId={}, parentArticleId={}",
                title, member.getId(), stage.getId(), parentArticleId != null ? parentArticleId : "없음");

        // 부모 게시글 조회
        Article parentArticle = null;
        if (parentArticleId != null) {
            parentArticle = getValidArticleOrNull(parentArticleId);
        }

        Article article = articleFactory.createArticleWithLinks(
                title, content, priority, deadLine,
                member, stage, parentArticle,
                linkList
        );

        Article savedArticle = articleProvider.store(article);
        log.info("[Service] 게시글 저장 완료: articleId={}", savedArticle.getId());
        return savedArticle;
    }

    private Article getValidArticleOrNull(Long articleId) {
        if (articleId == null) {
            return null;
        }

        return articleProvider.findById(articleId)
                .orElseThrow(() -> {
                    log.warn("[Service] (부모) 게시글을 찾을 수 없음: articleId={}", articleId);
                    return new GeneralException(ArticleErrorCode.PARENT_ARTICLE_NOT_FOUND);
                });
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
     * @return 해당 조건에 맞는 게시글 리스트
     */
    public Page<ArticleListViewResponse> getAllArticles(Long userId, String userRole, Long projectId, ArticleSearchCondition articleSearchCondition, Pageable pageable) {
        Member member = memberService.findByIdAndIsDeletedFalse(userId);
        Project project = projectService.getValidProject(projectId);

        checkIfMemberIsAdminOrProjectMember(userRole, member, project);

        Page<Article> articles = articleProvider.searchArticles(projectId, articleSearchCondition, pageable);
        log.info("조건에 맞는 게시글 페이지 조회 완료. PageNumber={}, PageSize={}, TotalElements={}",
                articles.getNumber(), articles.getSize(), articles.getTotalElements());


        List<ArticleListViewResponse> articleDTOList = articles.stream()
                .map(ArticleListViewResponse::fromEntity)
                .toList();

        Map<Long, List<ArticleListViewResponse>> parentToChildMap = articleDTOList.stream()
                .filter(articleDTO -> articleDTO.getParentArticleId() != null)
                .collect(Collectors.groupingBy(ArticleListViewResponse::getParentArticleId));

        List<ArticleListViewResponse> finalArticleDTOList = articleDTOList.stream()
                .map(articleDTO -> addChildArticleToParent(articleDTO, parentToChildMap))
                .toList();

        return new PageImpl<>(finalArticleDTOList, pageable, articles.getTotalElements());
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
            return articleProvider.findByStageAndStage_Project(stage, project);
        }
        return articleProvider.findByStage_Project(project);
    }

    /**
     * 게시글을 ID로 검증
     * @param articleId 게시글 ID
     * @return 검증된 게시글
     */
    public Article validateArticle(Long articleId) {
        return articleProvider.findByIdAndIsDeletedFalseWithMemberAndCompanyUsingQuerydsl(articleId)
                .orElseThrow(() -> {
                    log.warn("유효하지 않은 게시물입니다");
                    return new GeneralException(ArticleErrorCode.INVALID_ARTICLE);
                });
    }

    public Page<MyArticleListResponse> getMyArticles(Long userId, Long projectId, Pageable pageable) {
        log.info("사용자 ID {}가 작성한 게시글 목록 조회 시작. 프로젝트 필터 ID: {}", userId, projectId != null ? projectId : "없음");

        // 1. 리포지토리 호출하여 Tuple 데이터 조회
        Page<Tuple> tuplePage = fetchMyArticlesData(userId, projectId, pageable);

        // 2. 조회된 Tuple 데이터를 DTO로 변환 (헬퍼 메서드 사용)
        Page<MyArticleListResponse> responsePage = convertToMyArticleListResponsePage(tuplePage);

        log.info("사용자 ID {} 작성 게시글 목록 조회 완료. 조회된 게시글 수: {}", userId, responsePage.getTotalElements());
        return responsePage;
    }

    private Page<Tuple> fetchMyArticlesData(Long userId, Long projectId, Pageable pageable) {
        return articleProvider.findMyArticlesData(userId, projectId, pageable);
    }

    private Page<MyArticleListResponse> convertToMyArticleListResponsePage(Page<Tuple> tuplePage) {
        if (tuplePage.isEmpty()) {
            log.info("변환할 게시글 데이터(Tuple)가 없습니다.");
            return Page.empty(tuplePage.getPageable());
        } else {
            log.debug("조회된 Tuple 데이터를 MyArticleListResponse DTO로 변환 시작. 변환 대상 수: {}", tuplePage.getNumberOfElements());
        }

        return tuplePage.map(this::mapTupleToMyArticleResponse);
    }

    private MyArticleListResponse mapTupleToMyArticleResponse(Tuple tuple) {
        // Tuple에서 데이터 추출
        Long articleId = tuple.get(0, Long.class);
        String title = tuple.get(1, String.class);
        Long projId = tuple.get(2, Long.class);
        String projName = tuple.get(3, String.class);
        Long stgId = tuple.get(4, Long.class);
        String stgName = tuple.get(5, String.class);
        LocalDateTime createdAt = tuple.get(6, LocalDateTime.class);

        // null 체크
        if (articleId == null) {
            log.error("mapTupleToMyArticleResponse - Tuple에서 필수 데이터 누락: tuple={}", tuple);
            throw new GeneralException(ArticleErrorCode.ARTICLE_DATA_CONVERSION_ERROR);
        }

        // DTO 생성
        return MyArticleListResponse.from(articleId, title, projId, projName, stgId, stgName, createdAt);
    }

    @Transactional
    public ArticleStatusUpdateResponse updateArticleStatus(Long userId, Long articleId, ArticleStatusUpdateRequest request) {
        log.info("게시글 상태 변경 시작: articleId={}, userId={}, newStatus={}",
                articleId, userId, request.getStatus());

        Article article = validateArticle(articleId);
        Member member = memberService.findMemberById(userId);

        if (member == null || !member.getId().equals(userId)) {
            log.warn("게시글 상태 변경 권한 없음: 요청자(ID:{})가 작성자(ID:{})가 아닙니다. Article ID: {}",
                    userId, (member != null ? member.getId() : "null"), articleId);
            throw new GeneralException(ArticleErrorCode.NO_PERMISSION_TO_MODIFY_ARTICLE);
        }
        log.debug("게시글 작성자 본인 확인 완료. User ID: {}", userId);

        article.changeStatus(request.getStatus());
        articleProvider.store(article);
        log.info("게시글 상태 변경 완료: articleID={}, newStatus={}", article.getId(), article.getStatus());

        return ArticleStatusUpdateResponse.from(article);
    }


}
