package com.soda.project.repository;

import com.soda.member.entity.Company;
import com.soda.member.enums.CompanyProjectRole;
import com.soda.project.entity.CompanyProject;
import com.soda.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyProjectRepository extends JpaRepository<CompanyProject, Long> {
    boolean existsByCompanyAndProject(Company company, Project project);

    Optional<CompanyProject> findByProjectAndCompanyProjectRole(Project project, CompanyProjectRole companyProjectRole);

    List<CompanyProject> findByProject(Project project);

    Optional<CompanyProject> findByCompanyAndProjectAndIsDeletedFalse(Company company, Project project);

    List<CompanyProject> findByProjectAndRoleAndIsDeletedFalse(Project project, CompanyProjectRole role);
}
