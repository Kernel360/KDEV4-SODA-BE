package com.soda.project;

import com.soda.member.Company;
import com.soda.member.CompanyProjectRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyProjectRepository extends JpaRepository<CompanyProject, Long> {
    boolean existsByCompanyAndProject(Company company, Project project);

    Optional<CompanyProject> findByProjectAndCompanyProjectRole(Project project, CompanyProjectRole companyProjectRole);

    List<CompanyProject> findByProject(Project project);
}
