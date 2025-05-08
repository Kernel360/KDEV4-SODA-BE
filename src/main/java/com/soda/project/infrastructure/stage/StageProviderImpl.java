package com.soda.project.infrastructure.stage;

import com.soda.global.response.GeneralException;
import com.soda.project.domain.Project;
import com.soda.project.domain.ProjectErrorCode;
import com.soda.project.domain.stage.Stage;
import com.soda.project.domain.stage.StageErrorCode;
import com.soda.project.domain.stage.StageProvider;
import com.soda.project.infrastructure.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class StageProviderImpl implements StageProvider {
    private final StageRepository stageRepository;
    private final ProjectRepository projectRepository;

    @Override
    public Stage store(Stage stage) {
        return stageRepository.save(stage);
    }

    @Override
    public List<Stage> storeAll(List<Stage> stages) {
        return stageRepository.saveAll(stages);
    }

    @Override
    public Optional<Stage> findById(Long id) {
        return stageRepository.findById(id);
    }

    @Override
    public List<Stage> findByProjectIdAndIsDeletedFalseOrderByStageOrderAsc(Long projectId) {
        return stageRepository.findByProjectIdAndIsDeletedFalseOrderByStageOrderAsc(projectId);
    }

    @Override
    public boolean existsByProjectAndNameAndIsDeletedFalseAndIdNot(Project project, String name, Long stageId) {
        return stageRepository.existsByProjectAndNameAndIsDeletedFalseAndIdNot(project, name, stageId);
    }

    @Override
    public Project getProjectOrThrow(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new GeneralException(ProjectErrorCode.PROJECT_NOT_FOUND));
    }

    @Override
    public Stage getStageOrThrow(Long stageId) {
        return stageRepository.findById(stageId)
                .orElseThrow(() -> new GeneralException(StageErrorCode.STAGE_NOT_FOUND));
    }

    @Override
    public int countByProjectAndIsDeletedFalse(Project project) {
        return stageRepository.countByProjectAndIsDeletedFalse(project);
    }
}