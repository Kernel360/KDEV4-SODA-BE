package com.soda.project;

import com.soda.global.response.GeneralException;
import com.soda.member.CompanyProjectRole;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class CompanyProjectService {
    private final CompanyProjectRepository companyProjectRepository;

    public void save(CompanyProject companyProject) {
        companyProjectRepository.save(companyProject);
    }

    public String getCompanyNameByRole(Project project, CompanyProjectRole role) {
        CompanyProject companyProject = companyProjectRepository.findByProjectAndCompanyProjectRole(project, role)
                .orElseThrow(() -> new GeneralException(ProjectErrorCode.COMPANY_NOT_FOUND));
        return companyProject.getCompany().getName();
    }
}
