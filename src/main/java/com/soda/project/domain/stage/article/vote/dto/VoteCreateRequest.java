package com.soda.project.domain.stage.article.vote.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class VoteCreateRequest {

    @NotBlank(message = "투표 제목은 필수입니다.")
    @Size(max = 255, message = "투표 제목은 최대 255자까지 가능합니다.")
    private String title;

    @Size(max = 20, message = "투표 항목은 최대 20개까지 가능합니다.")
    private List<String> voteItems;

    @NotNull(message = "복수 선택 허용 여부는 필수입니다.")
    private Boolean allowMultipleSelection;

    @NotNull(message = "텍스트 답변 허용 여부는 필수입니다.")
    private Boolean allowTextAnswer;

    @Future(message = "투표 마감 시간은 현재 시간 이후여야 합니다.")
    private LocalDateTime deadLine;

}
