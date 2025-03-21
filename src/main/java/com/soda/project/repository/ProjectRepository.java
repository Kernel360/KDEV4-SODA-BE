package com.soda.project.repository;

import com.soda.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    boolean existsByTitle(String title);

    List<Project> findByIsDeletedFalse();
}
