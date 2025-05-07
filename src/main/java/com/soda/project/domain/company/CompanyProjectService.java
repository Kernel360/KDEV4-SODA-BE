package com.soda.project.domain.company;

import com.soda.global.response.GeneralException;
import com.soda.member.domain.Company;
import com.soda.project.domain.Project;
import com.soda.project.domain.ProjectErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyProjectService {
    private final CompanyProjectProvider companyProjectProvider;

    public List<String> getCompanyNamesByRole(Project project, CompanyProjectRole role) {
        List<CompanyProject> companyProjects = companyProjectProvider.findByProjectAndCompanyProjectRoleAndIsDeletedFalse(project, role);

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

    /**
     * 특정 회사와 프로젝트의 관계에 대한 CompanyProjectRole을 반환
     */
    public CompanyProjectRole getCompanyRoleInProject(Company company, Project project) {
        Optional<CompanyProject> companyProjectOpt = companyProjectProvider.findByCompanyAndProjectAndIsDeletedFalse(company, project);
        return companyProjectOpt.map(CompanyProject::getCompanyProjectRole).orElse(null);
    }

    public void deleteCompanyFromProject(Project project, Long companyId) {

        CompanyProject companyProject = companyProjectProvider.findByProjectIdAndCompanyIdAndIsDeletedFalse(project.getId(), companyId)
                .orElseThrow(() -> new GeneralException(ProjectErrorCode.COMPANY_PROJECT_NOT_FOUND));

        companyProject.delete();
        log.info("프로젝트 ID {} 에서 회사 ID {} 삭제 완료", project.getId(), companyId);
    }

    public List<Long> getCompanyIdsByProjectAndRoleAndIsDeletedFalse(Project project, CompanyProjectRole role) {
        log.debug("삭제되지 않은 회사 ID 목록 조회 시작: projectId={}, role={}", project.getId(), role);

        List<Long> companyIds = companyProjectProvider.findCompanyIdsByProjectAndRoleAndIsDeletedFalse(project, role);
        log.debug("삭제되지 않은 회사 ID 목록 조회 완료: count={}", companyIds.size());
        return companyIds;
    }
}