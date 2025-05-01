package com.soda.project.domain.stage.request.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GetMemberRequestCondition {
    private Long projectId;
    private String keyword;
}
