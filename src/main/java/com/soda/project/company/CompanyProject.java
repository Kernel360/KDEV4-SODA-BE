package com.soda.project.company;

import com.soda.common.BaseEntity;
import com.soda.member.Company;
import com.soda.member.CompanyProjectRole;
import com.soda.project.Project;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class CompanyProject extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Enumerated(EnumType.STRING)
    private CompanyProjectRole companyProjectRole;

    @Builder(access = AccessLevel.PRIVATE)
    public CompanyProject(Company company, Project project, CompanyProjectRole companyProjectRole) {
        this.company = company;
        this.project = project;
        this.companyProjectRole = companyProjectRole;
    }

    public static CompanyProject createDevCompany(Company company, Project project) {
        return CompanyProject.builder()
                .company(company)
                .project(project)
                .companyProjectRole(CompanyProjectRole.DEV_COMPANY)
                .build();
    }

    public static CompanyProject createClientCompany(Company company, Project project) {
        return CompanyProject.builder()
                .company(company)
                .project(project)
                .companyProjectRole(CompanyProjectRole.CLIENT_COMPANY)
                .build();
    }

    public void delete() {
        this.markAsDeleted();
    }

    public void updateCompanyProject(Company company, Project project, CompanyProjectRole companyProjectRole) {
        this.company = company;
        this.project = project;
        this.companyProjectRole = companyProjectRole;
    }
}
