package com.soda.project.repository;

import com.soda.project.entity.Project;
import com.soda.project.enums.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    boolean existsByTitle(String title);

    List<Project> findByIsDeletedFalse();

    Optional<Project> findByIdAndIsDeletedFalse(Long projectId);

    boolean existsByIdAndIsDeletedFalse(Long projectId);

    List<Project> findByIdIn(List<Long> projectIds);

    Optional<Project> findByTitleAndIdNot(String title, Long projectId);

    // project status 별로 조회
    List<Project> findByStatusAndIsDeletedFalse(ProjectStatus status);
}
