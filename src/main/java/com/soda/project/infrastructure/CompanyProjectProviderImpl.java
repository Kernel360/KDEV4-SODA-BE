package com.soda.project.infrastructure;

import com.soda.project.domain.company.CompanyProject;
import com.soda.project.domain.company.CompanyProjectProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CompanyProjectProviderImpl implements CompanyProjectProvider {
    private final CompanyProjectRepository companyProjectRepository;

    @Override
    public Optional<CompanyProject> findByProjectIdAndCompanyIdAndIsDeletedFalse(Long projectId, Long companyId) {
        return companyProjectRepository.findByProjectIdAndCompanyIdAndIsDeletedFalse(projectId, companyId);
    }
}
