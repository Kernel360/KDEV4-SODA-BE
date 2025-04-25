package com.soda.article.dto.article;

import com.soda.article.enums.ArticleStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ArticleStatusUpdateRequest {

    @NotNull(message = "변경할 프로젝트 상태는 필수입니다.")
    private ArticleStatus status;

}
