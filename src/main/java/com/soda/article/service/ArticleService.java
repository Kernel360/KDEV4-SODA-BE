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
    public ArticleModifyResponse createArticle(ArticleModifyRequest request, UserDetailsImpl userDetails) {
        Long memberId = userDetails.getMember().getId();
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorCode.MEMBER_NOT_FOUND));

        Stage stage = stageRepository.findById(request.getStageId())
                .orElseThrow(() -> new GeneralException(ErrorCode.STAGE_NOT_FOUND));
        Project project = stage.getProject();

        boolean isMemberInProject = memberProjectRepository.existsByMemberAndProjectAndIsDeletedFalse(member, project);
        if (!isMemberInProject) {
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
}