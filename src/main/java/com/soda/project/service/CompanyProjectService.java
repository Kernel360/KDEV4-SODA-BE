package com.soda.project.service;

import com.soda.global.response.GeneralException;
import com.soda.member.entity.Company;
import com.soda.member.enums.CompanyProjectRole;
import com.soda.member.service.CompanyService;
import com.soda.project.domain.CompanyProjectDTO;
import com.soda.project.entity.CompanyProject;
import com.soda.project.entity.Project;
import com.soda.project.error.ProjectErrorCode;
import com.soda.project.repository.CompanyProjectRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class CompanyProjectService {
    private final CompanyProjectRepository companyProjectRepository;
    private final CompanyService companyService;
    private final ProjectService projectService;

    public void assignCompanyToProject(Long companyId, Long projectId, CompanyProjectRole role) {
        Company company = companyService.getCompany(companyId);
        Project project = projectService.getProjectById(projectId);

        if (!company.getIsDeleted() && !companyProjectRepository.existsByCompanyAndProject(company, project)) {
            CompanyProjectDTO companyProjectDTO = CompanyProjectDTO.builder()
                    .companyId(company.getId())
                    .projectId(project.getId())
                    .companyProjectRole(role)
                    .build();

            // DTO -> Entity
            CompanyProject companyProject = companyProjectDTO.toEntity(company, project, role);

            // 데이터베이스에 회사-프로젝트 관계 저장
            companyProjectRepository.save(companyProject);
        }
    }

    public CompanyProject findByProjectAndCompanyProjectRole(Project project, CompanyProjectRole role) {
        return companyProjectRepository.findByProjectAndCompanyProjectRole(project, role)
                .orElseThrow(() -> new GeneralException(ProjectErrorCode.COMPANY_NOT_FOUND));
    }

    public List<CompanyProject> findByProject(Project project) {
        return companyProjectRepository.findByProject(project);
    }

    public void saveAll(List<CompanyProject> companyProjects) {
        companyProjectRepository.saveAll(companyProjects);
    }

    public boolean existsByCompanyAndProject(Company company, Project project) {
        return companyProjectRepository.existsByCompanyAndProject(company, project);
    }
}
