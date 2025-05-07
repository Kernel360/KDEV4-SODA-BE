package com.soda.project.infrastructure.company;

import com.soda.project.domain.Project;
import com.soda.project.domain.company.CompanyProjectRole;

import java.util.List;

public interface CompanyProjectRepositoryCustom {
    List<Long> findCompanyIdsByProjectAndRoleAndIsDeletedFalse(Project project, CompanyProjectRole role);
}
