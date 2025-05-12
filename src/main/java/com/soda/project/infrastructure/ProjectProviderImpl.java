package com.soda.project.infrastructure;

import com.querydsl.core.Tuple;
import com.soda.project.domain.Project;
import com.soda.project.domain.ProjectProvider;
import com.soda.project.interfaces.dto.ProjectListResponse;
import com.soda.project.interfaces.dto.ProjectSearchCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Override
    public Page<ProjectListResponse> searchProjects(ProjectSearchCondition request, Pageable pageable) {
        return projectRepository.searchProjects(request, pageable);
    }

    @Override
    public Page<Tuple> findMyProjectsData(ProjectSearchCondition request, Long userId, Pageable pageable) {
        return projectRepository.findMyProjectsData(request, userId,pageable);
    }

    @Override
    public Page<Tuple> findMyCompanyProjectsData(Long userId, Long companyId, Pageable pageable) {
        return projectRepository.findMyCompanyProjectsData(userId, companyId, pageable);
    }

    @Override
    public void delete(Project project) {
        project.delete();
        projectRepository.save(project);
    }
}
