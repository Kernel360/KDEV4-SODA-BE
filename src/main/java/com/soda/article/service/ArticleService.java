package com.soda.article.service;

import com.soda.article.domain.article.*;
import com.soda.article.entity.Article;
import com.soda.article.entity.ArticleFile;
import com.soda.article.entity.ArticleLink;
import com.soda.article.enums.ArticleStatus;
import com.soda.article.error.ArticleErrorCode;
import com.soda.article.repository.ArticleRepository;
import com.soda.global.response.GeneralException;
import com.soda.member.entity.Member;
import com.soda.member.service.MemberService;
import com.soda.project.entity.Project;
import com.soda.project.entity.Stage;
import com.soda.project.error.ProjectErrorCode;
import com.soda.project.service.MemberProjectService;
import com.soda.project.service.ProjectService;
import com.soda.project.service.StageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    /**
     * 게시글 생성하기
     * @param request 생성할 게시글의 상세 정보
     * @param userId 게시글을 생성하는 사용자 ID
     * @param userRole 게시글을 생성하는 사용자의 역할
     * @return 생성된 게시글의 정보
     */
    @Transactional
    public ArticleCreateResponse createArticle(ArticleCreateRequest request, Long userId, String userRole) {
        Member member = memberService.findByIdAndIsDeletedFalse(userId);
        Project project = projectService.getValidProject(request.getProjectId());
        Stage stage = validateStage(request.getStageId(), project);

        checkMemberInProject(userRole, member, project);

        Article parentArticle = null;
        if (request.getParentArticleId() != null) {
            parentArticle = articleRepository.findById(request.getParentArticleId())
                    .orElseThrow(() -> new GeneralException(ArticleErrorCode.PARENT_ARTICLE_NOT_FOUND));
        }

        validateFileAndLinkSize(request.getFileList(), request.getLinkList());

        Article article = request.toEntity(member, stage, parentArticle);
        article = articleRepository.save(article);

        // file & link 저장
        articleFileService.processFiles(request.getFileList(), article);
        articleLinkService.processLinks(request.getLinkList(), article);

        return ArticleCreateResponse.fromEntity(article);
    }

    /**
     * 기존 게시글 수정
     * @param userId 수정하는 사용자 ID
     * @param userRole 수정하는 사용자의 역할
     * @param articleId 수정할 게시글의 ID
     * @param request 수정할 게시글의 새로운 정보
     * @return 수정된 게시글의 정보
     */
    @Transactional
    public ArticleModifyResponse updateArticle(Long userId, String userRole, Long articleId, ArticleModifyRequest request) {
        Member member = memberService.findByIdAndIsDeletedFalse(userId);
        Project project = projectService.getValidProject(request.getProjectId());

        checkMemberInProject(userRole, member, project);

        Article article = findArticleById(articleId);

        validateFileAndLinkSize(request.getFileList(), request.getLinkList());

        article.updateArticle(request.getTitle(), request.getContent(), request.getPriority(), request.getDeadLine());

        // 기존 파일 및 링크 삭제
        articleFileService.deleteFiles(articleId, article);
        articleLinkService.deleteLinks(articleId, article);

        // 새 파일 및 링크 추가 또는 복원
        articleFileService.processFiles(request.getFileList(), article);
        articleLinkService.processLinks(request.getLinkList(), article);

        return ArticleModifyResponse.fromEntity(article);
    }

    /**
     * 게시글의 파일과 링크 크기 검증
     * @param fileList 게시글에 첨부된 파일 리스트
     * @param linkList 게시글에 첨부된 링크 리스트
     */
    private void validateFileAndLinkSize(List<ArticleFileDTO> fileList, List<ArticleLinkDTO> linkList) {
        if (fileList != null && fileList.size() > 10) {
            throw new GeneralException(ArticleErrorCode.INVALID_INPUT);
        }

        if (linkList != null && linkList.size() > 10) {
            throw new GeneralException(ArticleErrorCode.INVALID_INPUT);
        }
    }

    /**
     * 게시글 삭제
     * @param projectId 게시글이 속한 프로젝트 ID
     * @param userId 삭제를 요청하는 사용자 ID
     * @param userRole 삭제를 요청하는 사용자의 역할
     * @param articleId 삭제할 게시글의 ID
     */
    @Transactional
    public void deleteArticle(Long projectId, Long userId, String userRole, Long articleId) {
        Member member = memberService.findByIdAndIsDeletedFalse(userId);
        Project project = projectService.getValidProject(projectId);

        checkMemberInProject(userRole, member, project);

        Article article = findArticleById(articleId);
        validateArticleNotDeleted(article);

        // 게시글 삭제
        article.delete();

        // 연관된 파일 및 링크 삭제
        articleFileService.deleteFiles(articleId, article);
        articleLinkService.deleteLinks(articleId, article);
    }

    /**
     * 단계가 프로젝트에 적합한지 검증
     * @param stageId 단계 ID
     * @param project 프로젝트
     * @return 검증된 단계
     */
    private Stage validateStage(Long stageId, Project project) {
        Stage stage = stageService.findById(stageId);

        if (!stage.getProject().equals(project)) {
            throw new GeneralException(ProjectErrorCode.INVALID_STAGE_FOR_PROJECT);
        }

        return stage;
    }

    /**
     * ID로 게시글 찾기
     * @param articleId 게시글 ID
     * @return 찾은 게시글
     */
    private Article findArticleById(Long articleId) {
        return articleRepository.findByIdAndIsDeletedFalse(articleId)
                .orElseThrow(() -> new GeneralException(ArticleErrorCode.INVALID_ARTICLE));
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

        checkMemberInProject(userRole, member, project);

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

        checkMemberInProject(userRole, member, project);

        Article article = findArticleById(articleId);

        return ArticleViewResponse.fromEntity(article);
    }

    /**
     * 사용자가 프로젝트의 일원이 맞는지 확인
     * @param userRole 사용자의 역할
     * @param member 사용자 정보
     * @param project 프로젝트 정보
     * @throws GeneralException 사용자가 해당 프로젝트의 일원이 아닌 경우 예외 발생
     */
    private void checkMemberInProject(String userRole, Member member, Project project) {
        if (!isAdminOrMember(userRole, member, project)) {
            throw new GeneralException(ProjectErrorCode.MEMBER_NOT_IN_PROJECT);
        }
    }

    /**
     * 사용자가 관리자 또는 프로젝트의 멤버인지 확인
     * @param userRole 사용자의 역할
     * @param member 사용자 정보
     * @param project 프로젝트 정보
     * @return 관리자/멤버일 경우 true, 그렇지 않으면 false
     */
    private boolean isAdminOrMember(String userRole, Member member, Project project) {
        if ("ADMIN".equalsIgnoreCase(userRole)) {
            return true;
        }
        return memberProjectService.existsByMemberAndProjectAndIsDeletedFalse(member, project);
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
            return articleRepository.findByIsDeletedFalseAndStageAndStage_Project(stage, project);
        }
        return articleRepository.findByIsDeletedFalseAndStage_Project(project);
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

}
