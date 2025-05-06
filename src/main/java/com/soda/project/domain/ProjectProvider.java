package com.soda.project.domain;

import java.util.Optional;

public interface ProjectProvider {
    Project store(Project project);

    Optional<Project> findByIdAndIsDeletedFalse(Long projectId);
}
