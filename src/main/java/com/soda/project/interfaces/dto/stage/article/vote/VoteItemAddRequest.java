package com.soda.project.interfaces.dto.stage.article.vote;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VoteItemAddRequest {

    @NotBlank(message = "투표 항목은 빈 상태로 추가할 수 없습니다")
    @Size(max = 255, message = "255자 이상 작성할 수 없습니다.")
    private String itemText;

}
