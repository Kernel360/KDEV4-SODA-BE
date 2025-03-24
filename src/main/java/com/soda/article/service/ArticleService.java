package com.soda.article.service;

import com.soda.article.domain.ArticleDTO;
import com.soda.article.domain.ArticleModifyRequest;
import com.soda.article.entity.Article;
import com.soda.article.enums.ArticleStatus;
import com.soda.article.enums.PriorityType;
import com.soda.article.repository.ArticleRepository;
import com.soda.global.response.ErrorCode;
import com.soda.global.response.GeneralException;
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

import java.time.LocalDateTime;
import java.util.List;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final MemberRepository memberRepository;
    private final MemberProjectRepository memberProjectRepository;
    private final StageRepository stageRepository;
    private final ProjectRepository projectRepository;

    @Transactional
    public ArticleDTO createArticle(ArticleModifyRequest request) {
        // 1. 해당 project에 포함된 member 인가?
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new RuntimeException("member not found"));

        // 2. 해당 stage가 존재하는가?
        Stage stage = stageRepository.findById(request.getStageId())
                .orElseThrow(() -> new GeneralException(ErrorCode.STAGE_NOT_FOUND));
        Project project = stage.getProject();

        boolean isMemberInProject = memberProjectRepository.existsByMemberAndProjectAndIsDeletedFalse(member, project);
        if(!isMemberInProject) {
            throw new GeneralException(ErrorCode.MEMBER_NOT_IN_PROJECT);
        }

        // 3. 글 생성 (필수 필드 title, content)
        PriorityType priority = request.getPriority();
        LocalDateTime deadLine = request.getDeadLine();
        List<Long> fileList = request.getFileList();
        List<Long> linkList = request.getLinkList();

        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new GeneralException(ErrorCode.INVALID_INPUT);
        }

        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new GeneralException(ErrorCode.INVALID_INPUT);
        }

        Article article = ArticleDTO.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .priority(priority != null ? priority : PriorityType.LOW)
                .deadline(deadLine)
                .status(ArticleStatus.PENDING)
                .memberId(request.getMemberId())
                .stageId(request.getStageId())
                .fileList(fileList)
                .linkList(linkList)
                .build().toEntity(member, stage);

        // 4. DB에 저장
        Article savedArticle = articleRepository.save(article);

        // 5. DTO return
        return ArticleDTO.fromEntity(savedArticle);
    }


}
