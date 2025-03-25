package com.soda.article.service;

import com.soda.article.domain.*;
import com.soda.article.entity.Article;
import com.soda.article.entity.ArticleFile;
import com.soda.article.entity.ArticleLink;
import com.soda.article.enums.ArticleStatus;
import com.soda.article.repository.ArticleFileRepository;
import com.soda.article.repository.ArticleLinkRepository;
import com.soda.article.repository.ArticleRepository;
import com.soda.global.response.ErrorCode;
import com.soda.global.response.GeneralException;
import com.soda.global.security.auth.UserDetailsImpl;
import com.soda.global.security.jwt.JwtTokenProvider;
import com.soda.member.entity.Member;
import com.soda.member.repository.MemberRepository;
import com.soda.project.entity.Project;
import com.soda.project.entity.Stage;
import com.soda.project.repository.MemberProjectRepository;
import com.soda.project.repository.ProjectRepository;
import com.soda.project.repository.StageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final MemberRepository memberRepository;
    private final MemberProjectRepository memberProjectRepository;
    private final StageRepository stageRepository;
    private final ProjectRepository projectRepository;
    private final ArticleFileRepository articleFileRepository;
    private final ArticleLinkRepository articleLinkRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public ArticleModifyResponse createArticle(Long projectId, ArticleModifyRequest request, UserDetailsImpl userDetails) {
        Member member = userDetails.getMember();

        // 특정 프로젝트를 조회 (프로젝트가 존재하지 않으면 예외 발생)
        Project project = projectRepository.findByIdAndIsDeletedFalse(projectId)
                .orElseThrow(() -> new GeneralException(ErrorCode.PROJECT_NOT_FOUND));
        Stage stage = stageRepository.findById(request.getStageId())
                .orElseThrow(() -> new GeneralException(ErrorCode.STAGE_NOT_FOUND));

        if (!stage.getProject().equals(project)) {
            throw new GeneralException(ErrorCode.INVALID_STAGE_FOR_PROJECT);
        }

        // 로그인한 사용자가 해당 프로젝트에 참여 중인지 체크
        boolean isMemberInProject = memberProjectRepository.existsByMemberAndProjectAndIsDeletedFalse(member, project);
        if (!isMemberInProject && !member.isAdmin()) {
            throw new GeneralException(ErrorCode.MEMBER_NOT_IN_PROJECT);
        }

        if (request.getFileList() != null && request.getFileList().size() > 10) {
            throw new GeneralException(ErrorCode.INVALID_INPUT);
        }

        if (request.getLinkList() != null && request.getLinkList().size() > 10) {
            throw new GeneralException(ErrorCode.INVALID_INPUT);
        }

        Article article = Article.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .priority(request.getPriority())
                .deadline(request.getDeadLine())
                .member(member)
                .stage(stage)
                .status(ArticleStatus.PENDING)  // 기본 상태는 PENDING
                .build();

        article = articleRepository.save(article);

        // fileList와 articleLinkList 처리 (최대 10개 제한)
        if (request.getFileList() != null) {
            for (ArticleFileDTO fileDTO : request.getFileList()) {
                ArticleFile file = ArticleFile.builder()
                        .name(fileDTO.getName())
                        .url(fileDTO.getUrl())
                        .article(article)
                        .build();
                articleFileRepository.save(file);
                article.getArticleFileList().add(file);
            }
        }

        if (request.getLinkList() != null) {
            for (ArticleLinkDTO linkDTO : request.getLinkList()) {
                ArticleLink link = ArticleLink.builder()
                        .urlAddress(linkDTO.getUrlAddress())
                        .urlDescription(linkDTO.getUrlDescription())
                        .article(article)
                        .build();
                articleLinkRepository.save(link);
                article.getArticleLinkList().add(link);
            }
        }

        article = articleRepository.save(article);

        return ArticleModifyResponse.builder()
                .title(article.getTitle())
                .content(article.getContent())
                .priority(article.getPriority())
                .deadLine(article.getDeadline())
                .memberName(article.getMember().getName())
                .stageId(article.getStage().getId())
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
    public List<ArticleListResponse> getAllArticles(UserDetailsImpl userDetails, Long projectId) {
        Member member = userDetails.getMember();

        // 특정 프로젝트를 조회 (프로젝트가 존재하지 않으면 예외 발생)
        Project project = projectRepository.findByIdAndIsDeletedFalse(projectId)
                .orElseThrow(() -> new GeneralException(ErrorCode.PROJECT_NOT_FOUND));

        // 로그인한 멤버가 해당 프로젝트의 멤버인지를 확인
        boolean isMemberInProject = memberProjectRepository.existsByMemberAndProjectAndIsDeletedFalse(member, project);
        if (!isMemberInProject && !member.isAdmin()) {
            throw new GeneralException(ErrorCode.MEMBER_NOT_IN_PROJECT);
        }

        // 프로젝트에 속한 삭제되지 않은 게시글 조회
        List<Article> articles = articleRepository.findByIsDeletedFalseAndStage_Project(project);

        return articles.stream()
                .map(article -> ArticleListResponse.builder()
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
                        .build())
                .collect(Collectors.toList());
    }

    public ArticleListResponse getArticle(Long projectId, UserDetailsImpl userDetails, Long articleId) {
        Member member = userDetails.getMember();

        // 특정 프로젝트를 조회 (프로젝트가 존재하지 않으면 예외 발생)
        Project project = projectRepository.findByIdAndIsDeletedFalse(projectId)
                .orElseThrow(() -> new GeneralException(ErrorCode.PROJECT_NOT_FOUND));

        // 로그인한 멤버가 해당 프로젝트의 멤버인지를 확인
        boolean isMemberInProject = memberProjectRepository.existsByMemberAndProjectAndIsDeletedFalse(member, project);
        if (!isMemberInProject && !member.isAdmin()) {
            throw new GeneralException(ErrorCode.MEMBER_NOT_IN_PROJECT);
        }

        Article article = articleRepository.findByIdAndIsDeletedFalse(articleId)
                .orElseThrow(() -> new GeneralException(ErrorCode.INVALID_ARTICLE));

        return ArticleListResponse.builder()
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