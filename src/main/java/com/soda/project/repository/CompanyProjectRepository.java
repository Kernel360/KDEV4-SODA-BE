package com.soda.project.repository;

import com.soda.member.entity.Company;
import com.soda.project.entity.CompanyProject;
import com.soda.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyProjectRepository extends JpaRepository<CompanyProject, Long> {
    boolean existsByCompanyAndProject(Company company, Project project);
}
