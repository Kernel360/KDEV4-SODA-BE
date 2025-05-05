package com.soda.project.interfaces.dto;

import com.soda.project.domain.ProjectStatus;
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
