package com.soda.project.domain.stage.article;

import com.querydsl.core.Tuple;
import com.soda.project.domain.stage.article.error.ArticleErrorCode;
import com.soda.project.domain.stage.article.error.VoteErrorCode;
import com.soda.project.domain.stage.article.vote.Vote;
import com.soda.project.domain.stage.article.vote.VoteService;
import com.soda.project.infrastructure.ArticleRepository;
import com.soda.common.link.service.LinkService;
import com.soda.global.log.data.annotation.LoggableEntityAction;
import com.soda.global.response.GeneralException;
import com.soda.member.entity.Company;
import com.soda.member.entity.Member;
import com.soda.project.domain.company.enums.CompanyProjectRole;
import com.soda.member.enums.MemberRole;
import com.soda.member.service.MemberService;
import com.soda.project.domain.Project;
import com.soda.project.domain.stage.Stage;
import com.soda.project.domain.error.ProjectErrorCode;
import com.soda.project.domain.company.CompanyProjectService;
import com.soda.project.domain.member.MemberProjectService;
import com.soda.project.domain.ProjectService;
import com.soda.project.domain.stage.StageService;
import com.soda.project.interfaces.dto.stage.article.*;
import com.soda.project.interfaces.dto.stage.article.vote.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
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
    private final CompanyProjectService companyProjectService;
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
     * @return 해당 조건에 맞는 게시글 리스트
     */
    public Page<ArticleListViewResponse> getAllArticles(Long userId, String userRole, Long projectId, ArticleSearchCondition articleSearchCondition, Pageable pageable) {
        Member member = memberService.findByIdAndIsDeletedFalse(userId);
        Project project = projectService.getValidProject(projectId);

        checkIfMemberIsAdminOrProjectMember(userRole, member, project);

        Page<Article> articles = articleRepository.searchArticles(projectId, articleSearchCondition, pageable);
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
        return articleRepository.findByIdAndIsDeletedFalseWithMemberAndCompanyUsingQuerydsl(articleId)
                .orElseThrow(() -> {
                    log.warn("유효하지 않은 게시물입니다");
                    return new GeneralException(ArticleErrorCode.INVALID_ARTICLE);
                });
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
        return articleRepository.findMyArticlesData(userId, projectId, pageable);
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

    public VoteViewResponse getVoteInfoForArticle(Long articleId) {
        Article article = validateArticle(articleId);
        Vote vote = article.getVote();

        if (vote == null || vote.getIsDeleted()) {
            log.warn("[ArticleService] 게시글 ID {} 에 연결된 활성 투표가 없습니다.", articleId);
            // vote 없으면 null
            return null;
        }

        log.info("[ArticleService] 게시글 ID {} 투표 정보 조회 완료.", articleId);
        return VoteViewResponse.from(vote);
    }

    @Transactional
    public VoteSubmitResponse submitVoteForArticle(Long articleId, Long userId, String userRole, VoteSubmitRequest request) {
        log.info("[투표 제출 시작] 게시글 ID: {}, 사용자 ID: {}, 역할(문자열): {}", articleId, userId, userRole);
        Article article = validateArticle(articleId);

        Vote vote = article.getVote();
        Long voteId = vote.getId();
        log.debug("[투표 제출] 게시글 ID {} 에 연결된 투표 ID: {}", articleId, voteId);

        Stage stage = article.getStage();
        Project project = stage.getProject();

        Member currentUser = memberService.findMemberByIdAndCompany(userId); // 로그인한 사용자 확인
        Member author = article.getMember(); // 게시글 작성자 확인

        // 작성자 본인 투표 금지
        if (userId.equals(author.getId())) {
            log.warn("작성자(ID:{})가 자신의 게시글(ID:{}) 투표에 참여하려고 시도했습니다.", userId, articleId);
            throw new GeneralException(VoteErrorCode.CANNOT_VOTE_ON_OWN_ARTICLE);
        }

        // 투표 가능한지 유효성 검사
        Company authorMemberCompany = author.getCompany();

        CompanyProjectRole currentUserProjectRole =null;
        CompanyProjectRole authorCompanyProjectRole = null;

        if (currentUser.getRole() != MemberRole.ADMIN) {
            Company currentMemberCompany = currentUser.getCompany();
            currentUserProjectRole = companyProjectService.getCompanyRoleInProject(currentMemberCompany, project);
        } else {
            log.info("투표자가 관리자이므로 회사 검증을 하지 않습니다.");
        }

        if (author.getRole() != MemberRole.ADMIN) {
            authorCompanyProjectRole = companyProjectService.getCompanyRoleInProject(authorMemberCompany, project);
        } else {
            log.info("작성자가 관리자 이므로 회사 역할 검증을 하지 않습니다.");
        }
        
        checkVotingPermission(author.getRole(), currentUser.getRole(), currentUserProjectRole, authorCompanyProjectRole);

        log.info("[투표 제출] VoteService 호출 시작 - voteId: {}, userId: {}", voteId, userId);
        VoteSubmitResponse response = voteService.processVoteSubmit(voteId, userId, request);

        return VoteSubmitResponse.from(response);
    }

    private void checkVotingPermission(MemberRole authorRole, MemberRole currentUserRole, CompanyProjectRole currentUserProjectRole, CompanyProjectRole authorCompanyProjectRole) {
        boolean permitted = false;

        // 작성자가 ADMIN > 고객사/개발사 둘 다 투표 가능
        if (authorRole == MemberRole.ADMIN || currentUserRole == MemberRole.ADMIN) {
            permitted = true;
            log.debug("[투표 권한 확인] 작성자가 ADMIN이므로 투표 허용됨.");
        } else {
            if (authorCompanyProjectRole == CompanyProjectRole.DEV_COMPANY && currentUserProjectRole == CompanyProjectRole.CLIENT_COMPANY) {
                permitted = true;
            } else if (authorCompanyProjectRole == CompanyProjectRole.CLIENT_COMPANY && currentUserProjectRole == CompanyProjectRole.DEV_COMPANY) {
                permitted = true;
            }
        }

        if (!permitted) {
            log.warn("[투표 권한 없음] 작성자 회사 역할: {}, 투표자 회사 역할: {}", authorCompanyProjectRole, currentUserProjectRole);
            throw new GeneralException(VoteErrorCode.VOTE_PERMISSION_DENIED);
        }
        log.debug("[투표 권한 확인 완료] 허용됨: 작성자 회사 역할({}), 투표자 회사 역할({})", authorCompanyProjectRole, currentUserProjectRole);
    }

    @Transactional
    public VoteItemAddResponse addVoteItem(Long articleId, Long userId, VoteItemAddRequest request) {
        log.info("[항목 추가 시작(ArticleService)] Article ID: {}, User ID: {}, Item Text: {}",
                articleId, userId, request.getItemText());


        Article article = validateArticle(articleId);
        Vote vote = article.getVote();
        Stage stage = article.getStage();
        Project project = stage.getProject();

        Member requester = memberService.findByIdAndIsDeletedFalse(userId); // 요청자 정보
        Member author = article.getMember();

        checkVoteItemAddPermission(requester, author, project);

        VoteItemAddResponse addedItem = voteService.addVoteItemToVote(vote, request.getItemText());
        log.info("[항목 추가 성공(ArticleService)] Article ID: {}, New Item ID: {}", articleId, addedItem.getItemId());
        return addedItem;
    }

    private void checkVoteItemAddPermission(Member requester, Member author, Project project) {
        // 1. 작성자 본인 확인
        if (requester.getId().equals(author.getId())) {
            log.debug("항목 추가 권한 확인: 작성자 본인이므로 허용.");
            return; // 작성자 본인은 통과
        }

        // 2. 작성자가 ADMIN인 경우 -> 모든 사용자(회사 역할 무관) 추가 허용
        if (requester.getRole() == MemberRole.ADMIN) {
            log.debug("항목 추가 권한 확인: 요청자(ID:{})가 ADMIN이므로 허용.", requester.getId());
            return;
        }

        // 2-2. 작성자가 ADMIN인 경우 (그리고 요청자는 ADMIN이 아님) -> 허용
        if (author.getRole() == MemberRole.ADMIN) {
            log.debug("항목 추가 권한 확인: 작성자가 ADMIN이므로 요청자(ID:{}, Role:{}) 허용.", requester.getId(), requester.getRole());
            return; 
        }

        // 3. 고객사/개발사 유효성 검증
        Company requesterCompany = requester.getCompany();
        Company authorCompany = author.getCompany();

        CompanyProjectRole requesterRole = companyProjectService.getCompanyRoleInProject(requesterCompany, project);
        CompanyProjectRole authorRole = companyProjectService.getCompanyRoleInProject(authorCompany, project);

        boolean permitted = false;
        if (authorRole == CompanyProjectRole.DEV_COMPANY && requesterRole == CompanyProjectRole.CLIENT_COMPANY) {
            permitted = true;
        } else if (authorRole == CompanyProjectRole.CLIENT_COMPANY && requesterRole == CompanyProjectRole.DEV_COMPANY) {
            permitted = true;
        }

        // 권한 없는 경우 예외 발생
        if (!permitted) {
            log.warn("항목 추가 권한 없음: 작성자 회사 역할({})과 요청자 회사 역할({})이 교차 조건 불만족.", authorRole, requesterRole);
            throw new GeneralException(VoteErrorCode.VOTE_PERMISSION_DENIED);
        }

        log.debug("항목 추가 권한 확인 완료 (교차 회사 역할): Author Role({}), Requester Role({})", authorRole, requesterRole);
    }

    public VoteResultResponse getVoteResults(Long articleId, Long userId) {
        log.info("[결과 조회 시작(ArticleService) - 조건 없음/빌더 사용] Article ID: {}, User ID: {}", articleId, userId);

        Article article = validateArticle(articleId);
        Vote vote = article.getVote();
        log.debug("게시글 및 투표 정보 확인 완료. Vote ID: {}", vote.getId());

        VoteResultResponse response = voteService.getVoteResultData(vote);

        log.info("[결과 조회 성공(ArticleService)] Article ID: {}, Vote ID: {}", articleId, response.getVoteId());
        return response;
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
        articleRepository.save(article);
        log.info("게시글 상태 변경 완료: articleID={}, newStatus={}", article.getId(), article.getStatus());

        return ArticleStatusUpdateResponse.from(article);
    }
}
