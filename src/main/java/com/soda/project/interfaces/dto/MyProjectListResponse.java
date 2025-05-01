package com.soda.project.interfaces.dto;

import com.soda.project.domain.company.enums.CompanyProjectRole;
import com.soda.project.domain.member.enums.MemberProjectRole;
import com.soda.project.domain.Project;
import com.soda.project.domain.enums.ProjectStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MyProjectListResponse {

    private Long projectId;
    private String title;
    private ProjectStatus status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private CompanyProjectRole companyProjectRole;
    private MemberProjectRole memberProjectRole;

    public static MyProjectListResponse from(Project project, CompanyProjectRole companyProjectRole, MemberProjectRole memberProjectRole) {
        return MyProjectListResponse.builder()
                .projectId(project.getId())
                .title(project.getTitle())
                .status(project.getStatus())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .companyProjectRole(companyProjectRole)
                .memberProjectRole(memberProjectRole)
                .build();
    }

}
