package com.soda.project.domain.company;

import java.util.Optional;

public interface CompanyProjectProvider {
    Optional<CompanyProject> findByProjectIdAndCompanyIdAndIsDeletedFalse(Long projectId, Long companyId);
}
