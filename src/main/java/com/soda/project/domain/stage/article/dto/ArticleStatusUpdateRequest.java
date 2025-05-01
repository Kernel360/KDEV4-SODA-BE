package com.soda.project.domain.stage.article.dto;

import com.soda.project.domain.stage.article.enums.ArticleStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ArticleStatusUpdateRequest {

    @NotNull(message = "변경할 프로젝트 상태는 필수입니다.")
    private ArticleStatus status;

}
