package com.soda.project.service;

import com.soda.global.response.GeneralException;
import com.soda.member.entity.Company;
import com.soda.project.dto.CompanyProjectDTO;
import com.soda.project.entity.CompanyProject;
import com.soda.project.entity.Project;
import com.soda.member.enums.CompanyProjectRole;
import com.soda.project.error.ProjectErrorCode;
import com.soda.project.repository.CompanyProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class CompanyProjectService {
    private final CompanyProjectRepository companyProjectRepository;

    public void assignCompanyToProject(Company company, Project project, CompanyProjectRole role) {
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

    public List<Company> getCompaniesByRole(Project project, CompanyProjectRole role) {
        return companyProjectRepository.findByProjectAndCompanyProjectRoleAndIsDeletedFalse(project, role)
                .stream()
                .map(CompanyProject::getCompany)
                .collect(Collectors.toList());
    }

    public void deleteCompanyProjects(Project project) {
        List<CompanyProject> companyProjects = companyProjectRepository.findByProject(project);
        companyProjects.forEach(CompanyProject::delete);
        companyProjectRepository.saveAll(companyProjects);
    }

    public List<String> getCompanyNamesByRole(Project project, CompanyProjectRole role) {
        List<CompanyProject> companyProjects = companyProjectRepository.findByProjectAndCompanyProjectRoleAndIsDeletedFalse(project, role);

        if(companyProjects.isEmpty()) {
            log.debug("해당 역할의 활성 회사 없음: projectId={}, role={}", project.getId(), role);
            return List.of();
        }

        List<String> companyNames = companyProjects.stream()
                .map(cp -> cp.getCompany().getName())
                .distinct()
                .toList();

        log.info("회사 이름 목록 조회 완료: projectId={}, role={}", project.getId(), role);
        return companyNames;
    }

    private CompanyProject findByProjectAndCompanyProjectRole(Project project, CompanyProjectRole role) {
        return companyProjectRepository.findByProjectAndCompanyProjectRole(project, role)
                .orElseThrow(() -> new GeneralException(ProjectErrorCode.COMPANY_NOT_FOUND));
    }

    /**
     * 특정 회사와 프로젝트의 관계에 대한 CompanyProjectRole을 반환합니다.
     * @param company 회사 엔티티
     * @param project 프로젝트 엔티티
     * @return CompanyProjectRole (CLIENT 또는 DEVELOPER). 관계가 없으면 null을 반환합니다.
     */
    public CompanyProjectRole getCompanyRoleInProject(Company company, Project project) {
        Optional<CompanyProject> companyProjectOpt = companyProjectRepository.findByCompanyAndProjectAndIsDeletedFalse(company, project);
        return companyProjectOpt.map(CompanyProject::getCompanyProjectRole).orElse(null);
    }
}