package com.soda.project.dto;

import com.soda.member.entity.Company;
import com.soda.member.enums.CompanyProjectRole;
import com.soda.project.entity.CompanyProject;
import com.soda.project.entity.Project;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CompanyProjectCommand {

    private final Long companyId;
    private final Long projectId;
    private final CompanyProjectRole companyProjectRole;

    @Builder
    public CompanyProjectCommand(Long companyId, Long projectId, CompanyProjectRole companyProjectRole) {
        this.companyId = companyId;
        this.projectId = projectId;
        this.companyProjectRole = companyProjectRole;
    }

    // Entity → DTO 변환
    public static CompanyProjectCommand fromEntity(CompanyProject companyProject) {
        return CompanyProjectCommand.builder()
                .companyId(companyProject.getCompany().getId())
                .projectId(companyProject.getProject().getId())
                .companyProjectRole(companyProject.getCompanyProjectRole())
                .build();
    }

    // DTO → Entity 변환
    public CompanyProject toEntity(Company company, Project project, CompanyProjectRole companyProjectRole) {
        return CompanyProject.builder()
                .company(company)
                .project(project)
                .companyProjectRole(companyProjectRole)
                .build();
    }
}
