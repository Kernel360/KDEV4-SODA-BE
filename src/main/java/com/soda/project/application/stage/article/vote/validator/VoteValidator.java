package com.soda.project.application.stage.article.vote.validator;

import com.soda.global.response.GeneralException;
import com.soda.member.entity.Company;
import com.soda.member.entity.Member;
import com.soda.member.enums.MemberRole;
import com.soda.project.domain.Project;
import com.soda.project.domain.company.CompanyProjectService;
import com.soda.project.domain.company.enums.CompanyProjectRole;
import com.soda.project.domain.stage.article.Article;
import com.soda.project.domain.stage.article.error.VoteErrorCode;
import com.soda.project.domain.stage.article.vote.Vote;
import com.soda.project.domain.stage.article.vote.VoteAnswerService;
import com.soda.project.domain.stage.article.vote.VoteItem;
import com.soda.project.interfaces.dto.stage.article.vote.VoteCreateRequest;
import com.soda.project.interfaces.dto.stage.article.vote.VoteSubmitRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class VoteValidator {
    private final VoteAnswerService voteAnswerService;
    private final CompanyProjectService companyProjectService;

    public void validateCreateRequest(VoteCreateRequest request) {
        boolean hasItems = !CollectionUtils.isEmpty(request.getVoteItems());
        boolean textAllowed = request.getAllowTextAnswer();

        // 1. 항목 선택 투표 시 항목 필수 검증
        if (!textAllowed && !hasItems) {
            log.warn("유효성 검증 실패: 항목 선택 투표에는 항목이 필수입니다.");
            throw new GeneralException(VoteErrorCode.VOTE_ITEM_REQUIRED);
        }

        // 2. 텍스트 답변 투표 시 항목 불가 검증
        if (textAllowed && hasItems) {
            log.warn("유효성 검증 실패: 텍스트 답변 투표에는 항목을 포함할 수 없습니다.");
            throw new GeneralException(VoteErrorCode.VOTE_CANNOT_HAVE_BOTH_ITEMS_AND_TEXT);
        }

        // 3. 항목 중복 검사 (항목이 있는 경우에만)
        if (hasItems) {
            List<String> itemTexts = request.getVoteItems();
            // Set 이용하여 중복 제거 후 크기 비교
            Set<String> distinctItems = new HashSet<>(itemTexts);
            if (distinctItems.size() != itemTexts.size()) {
                log.warn("유효성 검증 실패: 투표 항목 요청에 중복된 내용이 있습니다. Items: {}", itemTexts);
                throw new GeneralException(VoteErrorCode.VOTE_DUPLICATE_ITEM_TEXT);
            }
            log.debug("투표 항목 중복 검증 통과. {}개 항목 유효.", distinctItems.size());
        }

        log.debug("투표 생성 요청 데이터 유효성 검증 통과");
    }

    public void validateSubmission(Vote vote, Member submitter, Member articleAuthor, Project project, VoteSubmitRequest request) {
        log.debug("투표 제출 유효성 검증 시작: voteId={}, submitterId={}", vote.getId(), submitter.getId());

        // 1. 투표 마감 여부 확인
        validateVoteNotClosed(vote);

        // 2. 작성자 본인 투표 금지 확인
        validateNotOwnVote(submitter, articleAuthor);

        // 3. 중복 투표 확인
        validateNotAlreadyVoted(vote.getId(), submitter.getId());

        // 4. 투표 참여 권한 확인
        validateVotingPermission(articleAuthor, submitter, project);

        // 5. 입력값 유효성 검증 (텍스트/항목 선택 규칙)
        validateVoteInputByType(vote, request);

        // 6. 선택된 항목 유효성 검증 (항목 선택 투표 시)
        if (!vote.isAllowTextAnswer() && !CollectionUtils.isEmpty(request.getSelectedItemIds())) {
            validateSelectedItemsBelongToVote(vote, request.getSelectedItemIds());
        }

        log.debug("투표 제출 유효성 검증 통과: voteId={}, submitterId={}", vote.getId(), submitter.getId());
    }

    private void validateVoteNotClosed(Vote vote) {
        if (vote.isClosed()) {
            log.warn("유효성 검증 실패: Vote ID {} 는 이미 마감되었습니다.", vote.getId());
            throw new GeneralException(VoteErrorCode.VOTE_ALREADY_CLOSED);
        }
    }

    private void validateNotOwnVote(Member submitter, Member articleAuthor) {
        if (submitter.getId().equals(articleAuthor.getId())) {
            throw new GeneralException(VoteErrorCode.CANNOT_VOTE_ON_OWN_ARTICLE);
        }
    }

    private void validateNotAlreadyVoted(Long voteId, Long userId) {
        if (voteAnswerService.hasUserVoted(voteId, userId)) {
            log.warn("유효성 검증 실패: User ID {} 는 이미 Vote ID {} 에 투표했습니다.", userId, voteId);
            throw new GeneralException(VoteErrorCode.ALREADY_VOTED);
        }
    }

    private void validateVotingPermission(Member author, Member currentUser, Project project) {
        boolean permitted = false;
        MemberRole authorRole = author.getRole();
        MemberRole currentUserRole = currentUser.getRole();
        CompanyProjectRole currentUserProjectRole = null;
        CompanyProjectRole authorCompanyProjectRole = null;

        // 관리자는 항상 허용
        if (authorRole == MemberRole.ADMIN || currentUserRole == MemberRole.ADMIN) {
            permitted = true;
        } else {
            // 관리자가 아닌 경우 회사 역할 기반 검증
            Company currentMemberCompany = currentUser.getCompany();
            Company authorMemberCompany = author.getCompany();
            if (currentMemberCompany == null || authorMemberCompany == null) {
                log.warn("[투표 권한 확인 실패] 투표자 또는 작성자의 회사 정보가 없습니다. currentUserCompany={}, authorCompany={}", currentMemberCompany, authorMemberCompany);
                throw new GeneralException(VoteErrorCode.VOTE_PERMISSION_DENIED);
            }
            currentUserProjectRole = companyProjectService.getCompanyRoleInProject(currentMemberCompany, project);
            authorCompanyProjectRole = companyProjectService.getCompanyRoleInProject(authorMemberCompany, project);

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

    private void validateVoteInputByType(Vote vote, VoteSubmitRequest request) {
        boolean hasTextAnswer = StringUtils.hasText(request.getTextAnswer());
        boolean hasSelectedItems = !CollectionUtils.isEmpty(request.getSelectedItemIds());

        if (vote.isAllowTextAnswer()) { // 텍스트 답변 허용 투표
            if (!hasTextAnswer) {
                log.warn("입력 검증 실패: 텍스트 투표에는 답변이 필요합니다. Vote ID: {}", vote.getId());
                throw new GeneralException(VoteErrorCode.INVALID_VOTE_INPUT);
            }
            if (hasSelectedItems) { // 텍스트 투표인데 항목 선택 시
                log.warn("입력 검증 실패: 텍스트 투표에는 항목을 선택할 수 없습니다. Vote ID: {}", vote.getId());
                throw new GeneralException(VoteErrorCode.INVALID_VOTE_INPUT);
            }
            log.debug("Vote ID {} 텍스트 입력값 검증 통과", vote.getId());
        } else { // 항목 선택 투표
            if (!hasSelectedItems) {
                log.warn("입력 검증 실패: 항목 선택 투표에는 항목 선택이 필요합니다. Vote ID: {}", vote.getId());
                throw new GeneralException(VoteErrorCode.INVALID_VOTE_INPUT);
            }
            if (hasTextAnswer) { // 항목 투표인데 텍스트 답변 시
                log.warn("입력 검증 실패: 항목 선택 투표에는 텍스트 답변을 할 수 없습니다. Vote ID: {}", vote.getId());
                throw new GeneralException(VoteErrorCode.INVALID_VOTE_INPUT);
            }
            // 단일/다중 선택 제약 조건 검증
            if (!vote.isAllowMultipleSelection() && request.getSelectedItemIds().size() > 1) {
                log.warn("입력 검증 실패: 단일 선택 투표에 여러 항목({})이 선택되었습니다. Vote ID: {}", request.getSelectedItemIds().size(), vote.getId());
                throw new GeneralException(VoteErrorCode.INVALID_VOTE_INPUT);
            }
            log.debug("Vote ID {} 항목 선택 입력값 검증 통과", vote.getId());
        }
    }

    private void validateSelectedItemsBelongToVote(Vote vote, List<Long> selectedItemIds) {
        // 실제 투표 항목 ID Set 생성 (삭제되지 않은 항목만)
        Set<Long> actualItemIds = vote.getVoteItems().stream()
                .filter(item -> !item.getIsDeleted()) // 삭제되지 않은 항목만 고려
                .map(VoteItem::getId)
                .collect(Collectors.toSet());

        if (actualItemIds.isEmpty() && !selectedItemIds.isEmpty()) {
            throw new GeneralException(VoteErrorCode.INVALID_VOTE_ITEM);
        }

        for (Long selectedId : selectedItemIds) {
            if (!actualItemIds.contains(selectedId)) {
                log.warn("[항목 검증 실패] 선택된 항목 ID {} 는 Vote ID {} 에 속하지 않거나 삭제된 항목입니다.", selectedId, vote.getId());
                throw new GeneralException(VoteErrorCode.INVALID_VOTE_ITEM);
            }
        }
        log.debug("Vote ID {} 선택 항목 ID {} 유효성 검증 통과", vote.getId(), selectedItemIds);
    }

    /**
     * 투표 생성 권한 검증 (게시글 작성자)
     */
    public void validateVoteCreationPermission(Member requester, Article article) {
        if (!requester.getId().equals(article.getMember().getId())) {
            throw new GeneralException(VoteErrorCode.VOTE_PERMISSION_DENIED);
        }
        log.debug("투표 생성 권한 검증 통과");
    }

}
