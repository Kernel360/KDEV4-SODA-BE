package com.soda.project.application.stage.article.vote.validator;

import com.soda.global.response.GeneralException;
import com.soda.project.domain.stage.article.error.VoteErrorCode;
import com.soda.project.interfaces.dto.stage.article.vote.VoteCreateRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class VoteValidator {

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

}
