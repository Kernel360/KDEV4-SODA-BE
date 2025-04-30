package com.soda.project.repository;

import com.soda.member.entity.Company;
import com.soda.member.enums.CompanyProjectRole;
import com.soda.project.company.CompanyProject;
import com.soda.project.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyProjectRepository extends JpaRepository<CompanyProject, Long> {
    List<CompanyProject> findByProject(Project project);

    Optional<CompanyProject> findByCompanyAndProjectAndIsDeletedFalse(Company company, Project project);

    List<CompanyProject> findByProjectAndCompanyProjectRoleAndIsDeletedFalse(Project project, CompanyProjectRole role);

    Optional<CompanyProject> findByProjectIdAndCompanyIdAndIsDeletedFalse(Long projectId, Long companyId);

    @Query("SELECT cp.company.id FROM CompanyProject cp " +
            "WHERE cp.project = :project AND cp.companyProjectRole = :role AND cp.isDeleted = false")
    List<Long> findCompanyIdsByProjectAndRoleAndIsDeletedFalse(@Param("project") Project project,
                                                               @Param("role") CompanyProjectRole role);
}
