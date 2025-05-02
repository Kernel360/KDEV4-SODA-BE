package com.soda.project.infrastructure;

import com.querydsl.core.Tuple;
import com.soda.project.domain.dto.ProjectListResponse;
import com.soda.project.domain.dto.ProjectSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProjectRepositoryCustom {
    Page<Tuple> findMyProjectsData(ProjectSearchCondition projectSearchCondition, Long memberId, Pageable pageable);

    Page<Tuple> findMyCompanyProjectsData(Long memberId, Long companyId, Pageable pageable);

    Page<ProjectListResponse> searchProjects(ProjectSearchCondition request, Pageable pageable);

}
