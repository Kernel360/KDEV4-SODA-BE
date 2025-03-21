package com.soda.project.entity;

import com.soda.common.BaseEntity;
import com.soda.member.entity.Company;
import com.soda.member.enums.CompanyProjectRole;
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

    @Builder
    public CompanyProject(Company company, Project project, CompanyProjectRole companyProjectRole) {
        this.company = company;
        this.project = project;
        this.companyProjectRole = companyProjectRole;
    }
}
