package com.soda.project.infrastructure.company;

import com.soda.member.domain.company.Company;
import com.soda.project.domain.Project;
import com.soda.project.domain.company.CompanyProject;
import com.soda.project.domain.company.CompanyProjectProvider;
import com.soda.project.domain.company.CompanyProjectRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CompanyProjectProviderImpl implements CompanyProjectProvider {
    private final CompanyProjectRepository companyProjectRepository;

    @Override
    public Optional<CompanyProject> findByProjectIdAndCompanyIdAndIsDeletedFalse(Long projectId, Long companyId) {
        return companyProjectRepository.findByProjectIdAndCompanyIdAndIsDeletedFalse(projectId, companyId);
    }

    @Override
    public List<CompanyProject> findByProjectAndCompanyProjectRoleAndIsDeletedFalse(Project project, CompanyProjectRole role) {
        return companyProjectRepository.findByProjectAndCompanyProjectRoleAndIsDeletedFalse(project, role);
    }

    @Override
    public Optional<CompanyProject> findByCompanyAndProjectAndIsDeletedFalse(Company company, Project project) {
        return companyProjectRepository.findByCompanyAndProjectAndIsDeletedFalse(company, project);
    }

    @Override
    public List<Long> findCompanyIdsByProjectAndRoleAndIsDeletedFalse(Project project, CompanyProjectRole role) {
        return companyProjectRepository.findCompanyIdsByProjectAndRoleAndIsDeletedFalse(project, role);
    }
}
