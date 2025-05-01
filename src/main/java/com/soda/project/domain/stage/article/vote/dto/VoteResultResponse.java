package com.soda.project.domain.stage.article.vote.dto;

import com.soda.project.domain.stage.article.vote.Vote;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class VoteResultResponse {

    private Long voteId;
    private String title;
    private boolean allowMultipleSelection;
    private boolean allowTextAnswer;
    private LocalDateTime deadLine;
    private boolean isClosed;
    private int totalParticipants;
    private List<ItemResultDTO> itemResults;
    private List<String> textAnswers;

    @Getter
    @Builder
    public static class ItemResultDTO {
        private Long itemId;
        private String itemText;
        private int count;
        private double percentage;
    }

    public static VoteResultResponse from(Vote vote, int totalParticipants, Map<Long, Long> itemCounts, List<String> textAnswers) {
        boolean closed = vote.isClosed();
        List<ItemResultDTO> itemResultDTOs = null;

        if (!vote.isAllowTextAnswer() && vote.getVoteItems() != null) {
            final int effectiveTotalParticipants = totalParticipants > 0 ? totalParticipants : 1;
            itemResultDTOs = vote.getVoteItems().stream()
                    .filter(item -> !item.getIsDeleted())
                    .map(item -> {
                        long count = itemCounts.getOrDefault(item.getId(), 0L);
                        // 퍼센트 계산 시 분모가 0이 아닌지 확인
                        double percentage = (totalParticipants > 0) ? ((double) count / totalParticipants) * 100 : 0;
                        return ItemResultDTO.builder()
                                .itemId(item.getId())
                                .itemText(item.getText())
                                .count((int) count)
                                .percentage(Math.round(percentage * 10.0) / 10.0) // 소수점 첫째 자리
                                .build();
                    })
                    .toList();
        }

        return VoteResultResponse.builder()
                .voteId(vote.getId())
                .title(vote.getTitle())
                .allowMultipleSelection(vote.isAllowMultipleSelection())
                .allowTextAnswer(vote.isAllowTextAnswer())
                .deadLine(vote.getDeadLine())
                .isClosed(closed)
                .totalParticipants(totalParticipants)
                .itemResults(itemResultDTOs)
                .textAnswers(textAnswers)
                .build();
    }
}
