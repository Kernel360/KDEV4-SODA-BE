package com.soda.project.domain.company;

import com.soda.member.domain.Company;
import com.soda.project.domain.Project;

import java.util.List;
import java.util.Optional;

public interface CompanyProjectProvider {
    Optional<CompanyProject> findByProjectIdAndCompanyIdAndIsDeletedFalse(Long projectId, Long companyId);

    List<CompanyProject> findByProjectAndCompanyProjectRoleAndIsDeletedFalse(Project project, CompanyProjectRole role);

    Optional<CompanyProject> findByCompanyAndProjectAndIsDeletedFalse(Company company, Project project);

    List<Long> findCompanyIdsByProjectAndRoleAndIsDeletedFalse(Project project, CompanyProjectRole role);
}
