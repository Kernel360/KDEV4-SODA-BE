package com.soda.project.domain;

import com.querydsl.core.Tuple;
import com.soda.project.interfaces.dto.ProjectListResponse;
import com.soda.project.interfaces.dto.ProjectSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ProjectProvider {
    Project store(Project project);

    Optional<Project> findByIdAndIsDeletedFalse(Long projectId);

    Page<ProjectListResponse> searchProjects(ProjectSearchCondition projectSearchCondition, Pageable pageable);

    Page<Tuple> findMyProjectsData(ProjectSearchCondition projectSearchCondition, Long userId, Pageable pageable);

    Page<Tuple> findMyCompanyProjectsData(Long userId, Long companyId, Pageable pageable);

    void delete(Project project);

    Optional<Project> findById(Long projectId);
}
