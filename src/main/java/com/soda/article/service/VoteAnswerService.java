package com.soda.article.service;

import com.soda.article.entity.Vote;
import com.soda.article.entity.VoteAnswer;
import com.soda.article.error.VoteErrorCode;
import com.soda.article.repository.VoteAnswerRepository;
import com.soda.global.response.GeneralException;
import com.soda.member.entity.Member;
import com.soda.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VoteAnswerService {
    private final VoteAnswerRepository voteAnswerRepository;
    private final MemberService memberService;

    public boolean hasUserVoted(Long voteId, Long userId) {
        boolean exists = voteAnswerRepository.existsByVote_IdAndMember_Id(voteId, userId);
        log.debug("Vote ID {} 에 대한 User ID {} 의 투표 존재 여부 확인: {}", voteId, userId, exists);

        return exists;
    }

    @Transactional
    public VoteAnswer createAndSaveVoteAnswer(Vote vote, Long userId, String textAnswer) {
        // 1. 투표 마감 여부 확인
        if (vote.isClosed()) {
            log.warn("Vote ID {} 는 이미 마감되었습니다.", vote.getId());
            throw new GeneralException(VoteErrorCode.VOTE_ALREADY_CLOSED);
        }

        // 2. 텍스트 답변 허용 여부 확인
        boolean hasText = StringUtils.hasText(textAnswer);
        if (hasText && !vote.isAllowTextAnswer()) {
            log.warn("Vote ID {} 는 텍스트 답변을 허용하지 않습니다.", vote.getId());
            throw new GeneralException(VoteErrorCode.VOTE_TEXT_ANSWER_NOT_ALLOWED);
        }

        // 3. Member
        Member member = memberService.findMemberById(userId);

        VoteAnswer voteAnswer = VoteAnswer.builder()
                .vote(vote)
                .member(member)
                .textAnswer(textAnswer)
                .build();

        VoteAnswer savedAnswer = voteAnswerRepository.save(voteAnswer);
        log.info("VoteAnswer 저장 완료 (VoteAnswerService). Answer ID: {}", savedAnswer.getId());
        return savedAnswer;
    }
}
