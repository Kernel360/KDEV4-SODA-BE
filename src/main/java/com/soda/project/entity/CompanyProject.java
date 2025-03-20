package com.soda.project.entity;

import com.soda.common.BaseEntity;
import com.soda.member.entity.Company;
import com.soda.member.enums.CompanyProjectRole;
import jakarta.persistence.*;
import lombok.Getter;

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

}
