package com.soda.project.infrastructure.company;

import com.soda.member.domain.company.Company;
import com.soda.project.domain.Project;
import com.soda.project.domain.company.CompanyProject;
import com.soda.project.domain.company.CompanyProjectRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyProjectRepository extends JpaRepository<CompanyProject, Long>, CompanyProjectRepositoryCustom {
    List<CompanyProject> findByProject(Project project);

    Optional<CompanyProject> findByCompanyAndProjectAndIsDeletedFalse(Company company, Project project);

    List<CompanyProject> findByProjectAndCompanyProjectRoleAndIsDeletedFalse(Project project, CompanyProjectRole role);

    Optional<CompanyProject> findByProjectIdAndCompanyIdAndIsDeletedFalse(Long projectId, Long companyId);

}
