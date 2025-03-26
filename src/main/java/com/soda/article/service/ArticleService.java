package com.soda.article.service;

import com.soda.article.domain.*;
import com.soda.article.entity.Article;
import com.soda.article.entity.ArticleFile;
import com.soda.article.entity.ArticleLink;
import com.soda.article.enums.ArticleStatus;
import com.soda.article.error.ArticleErrorCode;
import com.soda.article.repository.ArticleFileRepository;
import com.soda.article.repository.ArticleLinkRepository;
import com.soda.article.repository.ArticleRepository;
import com.soda.global.response.GeneralException;
import com.soda.global.security.auth.UserDetailsImpl;
import com.soda.member.entity.Member;
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

    @Transactional
    public ArticleCreateResponse createArticle(ArticleCreateRequest request, UserDetailsImpl userDetails) {
        Member member = userDetails.getMember();
        Project project = validateProject(request.getProjectId());
        Stage stage = validateStage(request.getStageId(), project);
        validateMemberInProject(project.getId(), member);

        validateFileAndLinkSize(request);

        Article article = saveArticle(request, member, stage);

        // file & link 저장
        processFilesAndLinks(request, article);

        article = articleRepository.save(article);

        return buildArticleModifyResponse(article);
    }

    // article 수정
    @Transactional
    public ArticleCreateResponse updateArticle(Long projectId, UserDetailsImpl userDetails, Long articleId, ArticleCreateRequest request) {
        Member member = userDetails.getMember();
        Project project = validateProject(projectId);
        validateMemberInProject(project.getId(), member);

        Article article = findArticleById(articleId);

        validateFileAndLinkSize(request);

        article.updateArticle(request.getTitle(), request.getContent(), request.getPriority(), request.getDeadLine());

        // 기존 파일 및 링크 삭제
        processDeletionForFilesAndLinks(articleId, article);

        // 새 파일 및 링크 추가 또는 복원
        processFilesAndLinks(request, article);

        article = articleRepository.save(article);

        return buildArticleModifyResponse(article);
    }

    // 게시글 삭제
    @Transactional
    public void deleteArticle(Long projectId, UserDetailsImpl userDetails, Long articleId) {
        Member member = userDetails.getMember();
        validateMemberInProject(projectId, member);

        Article article = findArticleById(articleId);
        validateArticleNotDeleted(article);

        // 게시글 삭제
        article.delete();

        // 연관된 파일 및 링크 삭제
        processDeletionForFilesAndLinks(articleId, article);

        articleRepository.save(article);
    }

    // 게시글 저장
    private Article saveArticle(ArticleCreateRequest request, Member member, Stage stage) {
        Article article = Article.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .priority(request.getPriority())
                .deadline(request.getDeadLine())
                .member(member)
                .stage(stage)
                .status(ArticleStatus.PENDING)  // 기본 상태는 PENDING
                .build();

        return articleRepository.save(article);
    }

    // 공통된 파일 및 링크 처리 로직
    private void processFilesAndLinks(ArticleCreateRequest request, Article article) {
        if (request.getFileList() != null) {
            request.getFileList().forEach(fileDTO -> {
                ArticleFile file = processFile(fileDTO, article);
                articleFileRepository.save(file);
                article.getArticleFileList().add(file);
            });
        }

        if (request.getLinkList() != null) {
            request.getLinkList().forEach(linkDTO -> {
                ArticleLink link = processLink(linkDTO, article);
                articleLinkRepository.save(link);
                article.getArticleLinkList().add(link);
            });
        }
    }

    // 기존 파일 및 링크 삭제
    private void processDeletionForFilesAndLinks(Long articleId, Article article) {
        List<ArticleFile> existingFiles = articleFileRepository.findByArticleId(articleId);
        existingFiles.forEach(ArticleFile::delete);
        article.getArticleFileList().removeIf(existingFiles::contains);

        List<ArticleLink> existingLinks = articleLinkRepository.findByArticleId(articleId);
        existingLinks.forEach(ArticleLink::delete);
        article.getArticleLinkList().removeIf(existingLinks::contains);
    }

    // 파일 처리 (새로운 파일 추가 또는 복원)
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

    // 링크 처리 (새로운 링크 추가 또는 복원)
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

    // 파일과 링크의 수가 10개를 초과하는지 체크
    private void validateFileAndLinkSize(ArticleCreateRequest request) {
        if (request.getFileList() != null && request.getFileList().size() > 10) {
            throw new GeneralException(ArticleErrorCode.INVALID_INPUT);
        }

        if (request.getLinkList() != null && request.getLinkList().size() > 10) {
            throw new GeneralException(ArticleErrorCode.INVALID_INPUT);
        }
    }

    // 프로젝트 검증
    private Project validateProject(Long projectId) {
        return projectRepository.findByIdAndIsDeletedFalse(projectId)
                .orElseThrow(() -> new GeneralException(ProjectErrorCode.PROJECT_NOT_FOUND));

    }

    // 단계 검증
    private Stage validateStage(Long stageId, Project project) {
        Stage stage = stageRepository.findById(stageId)
                .orElseThrow(() -> new GeneralException(ProjectErrorCode.STAGE_NOT_FOUND));

        if (!stage.getProject().equals(project)) {
            throw new GeneralException(ProjectErrorCode.INVALID_STAGE_FOR_PROJECT);
        }

        return stage;
    }

    // 게시글 조회
    private Article findArticleById(Long articleId) {
        return articleRepository.findByIdAndIsDeletedFalse(articleId)
                .orElseThrow(() -> new GeneralException(ArticleErrorCode.INVALID_ARTICLE));
    }

    // 이미 삭제된 게시글 체크
    private void validateArticleNotDeleted(Article article) {
        if (article.getIsDeleted()) {
            throw new GeneralException(ArticleErrorCode.ARTICLE_ALREADY_DELETED);
        }
    }

    // 응답 객체 생성
    private ArticleCreateResponse buildArticleModifyResponse(Article article) {
        return ArticleCreateResponse.builder()
                .title(article.getTitle())
                .content(article.getContent())
                .priority(article.getPriority())
                .deadLine(article.getDeadline())
                .memberName(article.getMember().getName())
                .stageName(article.getStage().getName())
                .fileList(article.getArticleFileList().stream()
                        .map(file -> ArticleFileDTO.builder()
                                .name(file.getName())
                                .url(file.getUrl())
                                .build())
                        .collect(Collectors.toList()))
                .linkList(article.getArticleLinkList().stream()
                        .map(link -> ArticleLinkDTO.builder()
                                .urlAddress(link.getUrlAddress())
                                .urlDescription(link.getUrlDescription())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    // Article List 조회
    public List<ArticleViewResponse> getAllArticles(UserDetailsImpl userDetails, Long projectId) {
        Member member = userDetails.getMember();

        validateMemberInProject(projectId, member);
        Project project = projectRepository.findByIdAndIsDeletedFalse(projectId)
                .orElseThrow(() -> new GeneralException(ProjectErrorCode.PROJECT_NOT_FOUND));

        // 프로젝트에 속한 삭제되지 않은 게시글 조회
        List<Article> articles = articleRepository.findByIsDeletedFalseAndStage_Project(project);

        return articles.stream()
                .map(this::buildArticleViewResponse)
                .collect(Collectors.toList());
    }

    public ArticleViewResponse getArticle(Long projectId, UserDetailsImpl userDetails, Long articleId) {
        Member member = userDetails.getMember();

        validateMemberInProject(projectId, member);

        Article article = articleRepository.findByIdAndIsDeletedFalse(articleId)
                .orElseThrow(() -> new GeneralException(ArticleErrorCode.INVALID_ARTICLE));

        return buildArticleViewResponse(article);
    }

    // 프로젝트 및 멤버 검증 로직
    private void validateMemberInProject(Long projectId, Member member) {
        // 특정 프로젝트를 조회 (프로젝트가 존재하지 않으면 예외 발생)
        Project project = projectRepository.findByIdAndIsDeletedFalse(projectId)
                .orElseThrow(() -> new GeneralException(ProjectErrorCode.PROJECT_NOT_FOUND));

        // 로그인한 멤버가 해당 프로젝트의 멤버인지를 확인
        boolean isMemberInProject = memberProjectRepository.existsByMemberAndProjectAndIsDeletedFalse(member, project);
        if (!isMemberInProject && !member.isAdmin()) {
            throw new GeneralException(ProjectErrorCode.MEMBER_NOT_IN_PROJECT);
        }
    }

    // ArticleViewResponse 생성 로직
    private ArticleViewResponse buildArticleViewResponse(Article article) {
        return ArticleViewResponse.builder()
                .title(article.getTitle())
                .content(article.getContent())
                .priority(article.getPriority())
                .deadLine(article.getDeadline())
                .memberName(article.getMember().getName())
                .stageName(article.getStage().getName())
                .fileList(article.getArticleFileList().stream()
                        .map(file -> ArticleFileDTO.builder()
                                .name(file.getName())
                                .url(file.getUrl())
                                .build())
                        .collect(Collectors.toList()))
                .linkList(article.getArticleLinkList().stream()
                        .map(link -> ArticleLinkDTO.builder()
                                .urlAddress(link.getUrlAddress())
                                .urlDescription(link.getUrlDescription())
                                .build())
                        .collect(Collectors.toList()))
                .commentList(article.getCommentList().stream()
                        .map(CommentDTO::fromEntity)
                        .collect(Collectors.toList()))
                .build();
    }


}