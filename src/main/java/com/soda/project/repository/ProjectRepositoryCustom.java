package com.soda.project.repository;

import com.querydsl.core.Tuple;
import com.soda.project.dto.ProjectListWithStatsResponse;
import com.soda.project.dto.ProjectSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProjectRepositoryCustom {
    Page<Tuple> findMyProjectsData(ProjectSearchCondition projectSearchCondition, Long memberId, Pageable pageable);

    Page<Tuple> findMyCompanyProjectsData(Long memberId, Long companyId, Pageable pageable);

    Page<ProjectListWithStatsResponse> searchProjects(ProjectSearchCondition request, Pageable pageable);

}
