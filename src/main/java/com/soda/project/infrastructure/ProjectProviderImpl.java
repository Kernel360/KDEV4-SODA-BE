package com.soda.project.infrastructure;

import com.soda.project.domain.Project;
import com.soda.project.domain.ProjectProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProjectProviderImpl implements ProjectProvider {
    private final ProjectRepository projectRepository;

    @Override
    public Project store(Project project) {
        return projectRepository.save(project);
    }

    @Override
    public Optional<Project> findByIdAndIsDeletedFalse(Long projectId) {
        return projectRepository.findByIdAndIsDeletedFalse(projectId);
    }
}
