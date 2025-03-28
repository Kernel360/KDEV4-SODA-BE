package com.soda.project.repository;

import com.soda.project.entity.Project;
import com.soda.project.entity.Stage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StageRepository extends JpaRepository<Stage, Long> {
    List<Stage> findByProjectIdOrderByStageOrderAsc(Long projectId);

    List<Stage> findByProjectIdAndIsDeletedFalseOrderByStageOrderAsc(Long projectId);

    int countByProjectAndIsDeletedFalse(Project project);
}
