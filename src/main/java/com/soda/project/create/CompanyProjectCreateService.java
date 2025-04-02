package com.soda.project.create;

import com.soda.member.Company;
import com.soda.member.CompanyProjectRole;
import com.soda.project.CompanyProject;
import com.soda.project.CompanyProjectDTO;
import com.soda.project.CompanyProjectRepository;
import com.soda.project.Project;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
// todo: 여기있는 제약조건들이 엔티티안으로 이관되어야함
public class CompanyProjectCreateService {
    private final CompanyProjectRepository companyProjectRepository;

    public void assignCompanyToProject(Company company, Project project, CompanyProjectRole role) {
        if (!company.getIsDeleted() && !companyProjectRepository.existsByCompanyAndProject(company, project)) {
            CompanyProjectDTO companyProjectDTO = CompanyProjectDTO.builder()
                    .companyId(company.getId())
                    .projectId(project.getId())
                    .companyProjectRole(role)
                    .build();

            // DTO -> Entity
            CompanyProject companyProject =

            // 데이터베이스에 회사-프로젝트 관계 저장
            companyProjectRepository.save(companyProject);
        }
    }
}
