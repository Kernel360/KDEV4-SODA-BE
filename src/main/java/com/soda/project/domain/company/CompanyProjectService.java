package com.soda.project.domain.company;

import com.soda.global.response.GeneralException;
import com.soda.member.entity.Company;
import com.soda.project.domain.Project;
import com.soda.member.enums.CompanyProjectRole;
import com.soda.project.domain.error.ProjectErrorCode;
import com.soda.project.infrastructure.CompanyProjectRepository;
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
        // 입력 값 Null 체크 (최소한의 방어)
        if (company == null || project == null || role == null) {
            log.error("assignCompanyToProject 호출 오류: company, project, role 중 null 값 존재");
            throw new GeneralException(ProjectErrorCode.NOT_NULL);
        }

        log.info(">>> 회사-프로젝트 연결 생성 시작: Company ID={}, Project ID={}, Target Role={}",
                company.getId(), project.getId(), role);

        CompanyProject companyProject = CompanyProject.builder()
                .company(company)
                .project(project)
                .companyProjectRole(role)
                .build();

        CompanyProject savedEntry = companyProjectRepository.save(companyProject);

        log.info("회사-프로젝트 연결 생성 완료: Company ID={}, Project ID={}", company.getId(), project.getId());
        log.info("회사-프로젝트 연결/업데이트 완료: Company ID={}, Project ID={}", company.getId(), project.getId());
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

    public void deleteCompanyFromProject(Project project, Long companyId) {

        CompanyProject companyProject = companyProjectRepository.findByProjectIdAndCompanyIdAndIsDeletedFalse(project.getId(), companyId)
                .orElseThrow(() -> new GeneralException(ProjectErrorCode.COMPANY_PROJECT_NOT_FOUND));

        companyProject.delete();
        log.info("프로젝트 ID {} 에서 회사 ID {} 삭제 완료", project.getId(), companyId);
    }

    public List<Long> getCompanyIdsByProjectAndRoleAndIsDeletedFalse(Project project, CompanyProjectRole role) {
        log.debug("삭제되지 않은 회사 ID 목록 조회 시작: projectId={}, role={}", project.getId(), role);

        List<Long> companyIds = companyProjectRepository.findCompanyIdsByProjectAndRoleAndIsDeletedFalse(project, role);
        log.debug("삭제되지 않은 회사 ID 목록 조회 완료: count={}", companyIds.size());
        return companyIds;
    }
}