package com.soda.article.service;

import com.soda.article.domain.article.*;
import com.soda.article.entity.Article;
import com.soda.article.entity.ArticleFile;
import com.soda.article.entity.ArticleLink;
import com.soda.article.enums.ArticleStatus;
import com.soda.article.error.ArticleErrorCode;
import com.soda.article.repository.ArticleFileRepository;
import com.soda.article.repository.ArticleLinkRepository;
import com.soda.article.repository.ArticleRepository;
import com.soda.global.response.GeneralException;
import com.soda.member.entity.Member;
import com.soda.member.repository.MemberRepository;
import com.soda.project.entity.Project;
import com.soda.project.entity.Stage;
import com.soda.project.error.ProjectErrorCode;
import com.soda.project.repository.MemberProjectRepository;
import com.soda.project.repository.ProjectRepository;
import com.soda.project.repository.StageRepository;
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
    private final MemberProjectRepository memberProjectRepository;
    private final StageRepository stageRepository;
    private final ProjectRepository projectRepository;
    private final ArticleFileRepository articleFileRepository;
    private final ArticleLinkRepository articleLinkRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public ArticleCreateResponse createArticle(ArticleCreateRequest request, Long userId, String userRole) {
        Member member = validateMember(userId);
        Project project = validateProject(request.getProjectId());
        Stage stage = validateStage(request.getStageId(), project);

        checkMemberInProject(userRole, member, project);

        Article parentArticle = null;
        if (request.getParentArticleId() != null) {
            parentArticle = articleRepository.findById(request.getParentArticleId())
                    .orElseThrow(() -> new GeneralException(ArticleErrorCode.PARENT_ARTICLE_NOT_FOUND));
        }

        validateFileAndLinkSize(request);

        Article article = saveArticle(request, member, stage, parentArticle);

        // file & link 저장
        processFilesAndLinks(request, article);

        return ArticleCreateResponse.fromEntity(article);
    }

    @Transactional
    public ArticleModifyResponse updateArticle(Long userId, String userRole, Long articleId, ArticleModifyRequest request) {
        Member member = validateMember(userId);
        Project project = validateProject(request.getProjectId());

        checkMemberInProject(userRole, member, project);

        Article article = findArticleById(articleId);

        //validateFileAndLinkSize(request);

        article.updateArticle(request.getTitle(), request.getContent(), request.getPriority(), request.getDeadLine());

        // 기존 파일 및 링크 삭제
        processDeletionForFilesAndLinks(articleId, article);

        // 새 파일 및 링크 추가 또는 복원
        //processFilesAndLinks(request, article);

        //return buildArticleModifyResponse(article);
        return null;
    }

    @Transactional
    public void deleteArticle(Long projectId, Long userId, String userRole, Long articleId) {
        Member member = validateMember(userId);
        Project project = validateProject(projectId);

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

    private void processFilesAndLinks(ArticleCreateRequest request, Article article) {
        // 파일 처리
        if (request.getFileList() != null) {
            request.getFileList().forEach(fileDTO -> {
                ArticleFile file = processFile(fileDTO, article);
                articleFileRepository.save(file);
                article.getArticleFileList().add(file);
            });
        }

        // 링크 처리
        if (request.getLinkList() != null) {
            request.getLinkList().forEach(linkDTO -> {
                ArticleLink link = processLink(linkDTO, article);
                articleLinkRepository.save(link);
                article.getArticleLinkList().add(link);
            });
        }
    }

    private void processDeletionForFilesAndLinks(Long articleId, Article article) {
        deleteFilesAndLinks(articleId, article);
    }

    private void deleteFilesAndLinks(Long articleId, Article article) {
        List<ArticleFile> existingFiles = articleFileRepository.findByArticleId(articleId);
        existingFiles.forEach(ArticleFile::delete);
        article.getArticleFileList().removeIf(existingFiles::contains);

        List<ArticleLink> existingLinks = articleLinkRepository.findByArticleId(articleId);
        existingLinks.forEach(ArticleLink::delete);
        article.getArticleLinkList().removeIf(existingLinks::contains);
    }

    private ArticleFile processFile(ArticleFileDTO fileDTO, Article article) {
        ArticleFile file = articleFileRepository.findByArticleIdAndNameAndIsDeletedTrue(article.getId(), fileDTO.getName())
                .orElse(null);

        if (file != null) {
            file.reActive();
        } else {
            file = ArticleFile.builder()
                    .name(fileDTO.getName())
                    .url(fileDTO.getUrl())
                    .article(article)
                    .build();
        }

        return file;
    }

    private ArticleLink processLink(ArticleLinkDTO linkDTO, Article article) {
        ArticleLink link = articleLinkRepository.findByArticleIdAndUrlAddressAndIsDeletedTrue(article.getId(), linkDTO.getUrlAddress())
                .orElse(null);

        if (link != null) {
            link.reActive();
        } else {
            link = ArticleLink.builder()
                    .urlAddress(linkDTO.getUrlAddress())
                    .urlDescription(linkDTO.getUrlDescription())
                    .article(article)
                    .build();
        }

        return link;
    }

    private void validateFileAndLinkSize(ArticleCreateRequest request) {
        if (request.getFileList() != null && request.getFileList().size() > 10) {
            throw new GeneralException(ArticleErrorCode.INVALID_INPUT);
        }

        if (request.getLinkList() != null && request.getLinkList().size() > 10) {
            throw new GeneralException(ArticleErrorCode.INVALID_INPUT);
        }
    }

    private Project validateProject(Long projectId) {
        return projectRepository.findByIdAndIsDeletedFalse(projectId)
                .orElseThrow(() -> new GeneralException(ProjectErrorCode.PROJECT_NOT_FOUND));
    }

    private Stage validateStage(Long stageId, Project project) {
        Stage stage = stageRepository.findById(stageId)
                .orElseThrow(() -> new GeneralException(ProjectErrorCode.STAGE_NOT_FOUND));

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
        Member member = validateMember(userId);
        Project project = validateProject(projectId);

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
        Member member = validateMember(userId);
        Project project = validateProject(projectId);

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
        return memberProjectRepository.existsByMemberAndProjectAndIsDeletedFalse(member, project);
    }

    private Member validateMember(Long userId) {
        return memberRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ProjectErrorCode.MEMBER_NOT_FOUND));
    }

    private List<Article> getArticlesByStageAndProject(Long stageId, Project project) {
        if (stageId != null) {
            Stage stage = stageRepository.findById(stageId)
                    .orElseThrow(() -> new GeneralException(ProjectErrorCode.STAGE_NOT_FOUND));
            return articleRepository.findByIsDeletedFalseAndStageAndStage_Project(stage, project);
        }
        return articleRepository.findByIsDeletedFalseAndStage_Project(project);
    }

}
