package com.soda.article.service;

import com.soda.article.domain.article.*;
import com.soda.article.entity.Article;
import com.soda.article.entity.ArticleFile;
import com.soda.article.entity.ArticleLink;
import com.soda.article.enums.ArticleStatus;
import com.soda.article.error.ArticleErrorCode;
import com.soda.article.repository.ArticleRepository;
import com.soda.global.response.GeneralException;
import com.soda.member.Member;
import com.soda.member.MemberService;
import com.soda.project.Project;
import com.soda.project.Stage;
import com.soda.project.ProjectErrorCode;
import com.soda.project.MemberProjectService;
import com.soda.project.ProjectSearchService;
import com.soda.project.StageService;
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
    private final ProjectSearchService projectSearchService;
    private final ArticleFileService articleFileService;
    private final ArticleLinkService articleLinkService;
    private final MemberService memberService;

    @Transactional
    public ArticleCreateResponse createArticle(ArticleCreateRequest request, Long userId, String userRole) {
        Member member = memberService.findByIdAndIsDeletedFalse(userId);
        Project project = projectSearchService.getValidProject(request.getProjectId());
        Stage stage = validateStage(request.getStageId(), project);

        checkMemberInProject(userRole, member, project);

        Article parentArticle = null;
        if (request.getParentArticleId() != null) {
            parentArticle = articleRepository.findById(request.getParentArticleId())
                    .orElseThrow(() -> new GeneralException(ArticleErrorCode.PARENT_ARTICLE_NOT_FOUND));
        }

        validateFileAndLinkSize(request.getFileList(), request.getLinkList());

        Article article = saveArticle(request, member, stage, parentArticle);

        // file & link 저장
        processFilesAndLinks(request.getFileList(), request.getLinkList(), article);

        return ArticleCreateResponse.fromEntity(article);
    }

    @Transactional
    public ArticleModifyResponse updateArticle(Long userId, String userRole, Long articleId, ArticleModifyRequest request) {
        Member member = memberService.findByIdAndIsDeletedFalse(userId);
        Project project = projectSearchService.getValidProject(request.getProjectId());

        checkMemberInProject(userRole, member, project);

        Article article = findArticleById(articleId);

        validateFileAndLinkSize(request.getFileList(), request.getLinkList());

        article.updateArticle(request.getTitle(), request.getContent(), request.getPriority(), request.getDeadLine());

        // 기존 파일 및 링크 삭제
        processDeletionForFilesAndLinks(articleId, article);

        // 새 파일 및 링크 추가 또는 복원
        processFilesAndLinks(request.getFileList(), request.getLinkList(), article);

        return ArticleModifyResponse.fromEntity(article);
    }

    private void validateFileAndLinkSize(List<ArticleFileDTO> fileList, List<ArticleLinkDTO> linkList) {
        if (fileList != null && fileList.size() > 10) {
            throw new GeneralException(ArticleErrorCode.INVALID_INPUT);
        }

        if (linkList != null && linkList.size() > 10) {
            throw new GeneralException(ArticleErrorCode.INVALID_INPUT);
        }
    }

    private void processFilesAndLinks(List<ArticleFileDTO> fileList, List<ArticleLinkDTO> linkList, Article article) {
        if (fileList != null) {
            fileList.forEach(articleFileDTO -> {
                ArticleFile file = articleFileService.processFile(articleFileDTO, article);
                article.getArticleFileList().add(file);
            });
        }

        if (linkList != null) {
            linkList.forEach(articleLinkDTO -> {
                ArticleLink link = articleLinkService.processLink(articleLinkDTO, article);
                article.getArticleLinkList().add(link);
            });
        }
    }

    @Transactional
    public void deleteArticle(Long projectId, Long userId, String userRole, Long articleId) {
        Member member = memberService.findByIdAndIsDeletedFalse(userId);
        Project project = projectSearchService.getValidProject(projectId);

        checkMemberInProject(userRole, member, project);

        Article article = findArticleById(articleId);
        validateArticleNotDeleted(article);

        // 게시글 삭제
        article.delete();

        // 연관된 파일 및 링크 삭제
        processDeletionForFilesAndLinks(articleId, article);
    }

    private Article saveArticle(ArticleCreateRequest request, Member member, Stage stage, Article parentArticle) {
        Article article = Article.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .priority(request.getPriority())
                .deadline(request.getDeadLine())
                .member(member)
                .stage(stage)
                .status(ArticleStatus.PENDING)
                .parentArticle(parentArticle)
                .build();

        return articleRepository.save(article);
    }

    private void processDeletionForFilesAndLinks(Long articleId, Article article) {
        // file delete
        articleFileService.deleteFiles(articleId, article);
        // link delete
        articleLinkService.deleteLinks(articleId, article);
    }

    private Stage validateStage(Long stageId, Project project) {
        Stage stage = stageService.findById(stageId);

        if (!stage.getProject().equals(project)) {
            throw new GeneralException(ProjectErrorCode.INVALID_STAGE_FOR_PROJECT);
        }

        return stage;
    }

    private Article findArticleById(Long articleId) {
        return articleRepository.findByIdAndIsDeletedFalse(articleId)
                .orElseThrow(() -> new GeneralException(ArticleErrorCode.INVALID_ARTICLE));
    }

    private void validateArticleNotDeleted(Article article) {
        if (article.getIsDeleted()) {
            throw new GeneralException(ArticleErrorCode.ARTICLE_ALREADY_DELETED);
        }
    }

    public List<ArticleListViewResponse> getAllArticles(Long userId, String userRole, Long projectId, Long stageId) {
        Member member = memberService.findByIdAndIsDeletedFalse(userId);
        Project project = projectSearchService.getValidProject(projectId);

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

    // 답글을 게시글에 추가하는 재귀 메소드
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

    public ArticleViewResponse getArticle(Long projectId, Long userId, String userRole, Long articleId) {
        Member member = memberService.findByIdAndIsDeletedFalse(userId);
        Project project = projectSearchService.getValidProject(projectId);

        checkMemberInProject(userRole, member, project);

        Article article = findArticleById(articleId);

        return ArticleViewResponse.fromEntity(article);
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
        return memberProjectService.existsByMemberAndProjectAndIsDeletedFalse(member, project);
    }

    private List<Article> getArticlesByStageAndProject(Long stageId, Project project) {
        if (stageId != null) {
            Stage stage = stageService.findById(stageId);
            return articleRepository.findByIsDeletedFalseAndStageAndStage_Project(stage, project);
        }
        return articleRepository.findByIsDeletedFalseAndStage_Project(project);
    }

    public Article validateArticle(Long articleId) {
        return articleRepository.findByIdAndIsDeletedFalse(articleId)
                .orElseThrow(() -> new GeneralException(ArticleErrorCode.INVALID_ARTICLE));
    }

}
