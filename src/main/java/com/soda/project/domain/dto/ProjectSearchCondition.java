package com.soda.project.domain.dto;

import com.soda.project.domain.enums.ProjectStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProjectSearchCondition {

    private ProjectStatus status;
    private String keyword;

}
