package com.soda.project;

import com.soda.member.Company;
import com.soda.member.CompanyProjectRole;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CompanyProjectDTO {

    private Long companyId;
    private Long projectId;
    private CompanyProjectRole companyProjectRole;

    @Builder
    public CompanyProjectDTO (Long companyId, Long projectId, CompanyProjectRole companyProjectRole) {
        this.companyId = companyId;
        this.projectId = projectId;
        this.companyProjectRole = companyProjectRole;
    }

    // Entity → DTO 변환
    public static CompanyProjectDTO fromEntity(CompanyProject companyProject) {
        return CompanyProjectDTO.builder()
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
