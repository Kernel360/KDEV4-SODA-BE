package com.soda.project.repository;

import com.querydsl.core.Tuple;
import com.soda.project.dto.ProjectSearchCondition;
import com.soda.project.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProjectRepositoryCustom {
    Page<Tuple> findMyProjectsData(Long memberId, Pageable pageable);

    Page<Tuple> findMyCompanyProjectsData(Long memberId, Long companyId, Pageable pageable);

    Page<Project> searchProjects(ProjectSearchCondition request, Pageable pageable);
}
