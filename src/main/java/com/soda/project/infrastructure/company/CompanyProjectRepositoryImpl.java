package com.soda.project.infrastructure.company;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.soda.project.domain.Project;
import com.soda.project.domain.company.CompanyProjectRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.soda.project.domain.company.QCompanyProject.companyProject;

@Repository
@RequiredArgsConstructor
public class CompanyProjectRepositoryImpl implements CompanyProjectRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Long> findCompanyIdsByProjectAndRoleAndIsDeletedFalse(Project project, CompanyProjectRole role) {
        return queryFactory
                .select(companyProject.company.id)
                .from(companyProject)
                .where(
                        companyProject.project.eq(project),
                        companyProject.companyProjectRole.eq(role),
                        companyProject.isDeleted.isFalse()
                )
                .fetch();
    }
}
