package com.soda.project.interfaces.dto.article;

import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class VoteSubmitRequest {

    // 항목 선택 투표 시 선택한 항목 ID 목록 (단일 투표 시에는 크기 1)
    private List<Long> selectedItemIds;

    @Size(max = 1000, message = "답변은 최대 1000자까지 입력 가능합니다.")
    private String textAnswer;

}