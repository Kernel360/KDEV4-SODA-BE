package com.soda.project.repository;

import com.soda.project.entity.Project;
import com.soda.project.enums.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    // 전체 프로젝트 목록 최신순 조회
    Page<Project> findByIsDeletedFalse(Pageable pageable);

    Optional<Project> findByIdAndIsDeletedFalse(Long projectId);

    boolean existsByIdAndIsDeletedFalse(Long projectId);

    Page<Project> findByIdIn(List<Long> projectIds, Pageable pageable);

    Optional<Project> findByTitleAndIdNot(String title, Long projectId);

    // project status 별로 조회
    Page<Project> findByStatusAndIsDeletedFalse(@Param("status") ProjectStatus status, Pageable pageable);

}
