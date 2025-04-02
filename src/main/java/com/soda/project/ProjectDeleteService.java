package com.soda.project;

import com.soda.global.response.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProjectDeleteService {
    private final ProjectRepository projectRepository;

    @Transactional
    public void deleteProject(Long projectId) {
        Project project = projectRepository.findByIdAndIsDeletedFalse(projectId)
                .orElseThrow(() -> new GeneralException(ProjectErrorCode.PROJECT_NOT_FOUND));

        project.delete();

        //? 딜리트로 마크하는걸 보고 소프트 딜리트인줄 알았는데??
        projectRepository.delete(project);
    }
}
