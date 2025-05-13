package com.soda.member.infrastructure.company;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.soda.member.domain.company.Company;
import com.soda.member.domain.company.QCompany;
import com.soda.member.interfaces.dto.CompanyCreationStatRaw;
import com.soda.member.interfaces.dto.company.CompanyViewOption;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

import static com.soda.member.domain.company.QCompany.company;

@Repository
@RequiredArgsConstructor
public class CompanyRepositoryImpl implements CompanyRepositoryCustom {

        private final JPAQueryFactory queryFactory;

        @Override
        public List<CompanyCreationStatRaw> countCompaniesByDayRaw(LocalDateTime startDate, LocalDateTime endDate) {
                QCompany company = QCompany.company;

                NumberExpression<Integer> year = company.createdAt.year();
                NumberExpression<Integer> month = company.createdAt.month();
                NumberExpression<Integer> week = company.createdAt.week();
                NumberExpression<Integer> day = company.createdAt.dayOfMonth();

                return queryFactory
                                .select(Projections.constructor(CompanyCreationStatRaw.class,
                                                year,
                                                month,
                                                week,
                                                day,
                                                company.count().as("count")))
                                .from(company)
                                .where(
                                                company.createdAt.goe(startDate),
                                                company.createdAt.lt(endDate),
                                                company.isDeleted.isFalse())
                                .groupBy(year, month, week, day)
                                .orderBy(year.asc(), month.asc(), week.asc(), day.asc())
                                .fetch();
        }

        @Override
        public Page<Company> findAllCompaniesWithSearch(CompanyViewOption viewOption, String searchKeyword,
                        Pageable pageable) {
                BooleanExpression whereClause = createWhereClause(viewOption, searchKeyword);

                List<Company> companies = queryFactory
                                .selectFrom(company)
                                .where(whereClause)
                                .orderBy(company.createdAt.desc())
                                .offset(pageable.getOffset())
                                .limit(pageable.getPageSize())
                                .fetch();

                Long total = queryFactory
                                .select(company.count())
                                .from(company)
                                .where(whereClause)
                                .fetchOne();

                return new PageImpl<>(companies, pageable, total != null ? total : 0L);
        }

        private BooleanExpression createWhereClause(CompanyViewOption viewOption, String searchKeyword) {
                BooleanExpression baseClause = null;

                switch (viewOption) {
                        case ACTIVE -> baseClause = company.isDeleted.isFalse();
                        case DELETED -> baseClause = company.isDeleted.isTrue();
                        case ALL -> baseClause = null;
                }

                if (StringUtils.hasText(searchKeyword)) {
                        BooleanExpression searchClause = company.name.containsIgnoreCase(searchKeyword)
                                        .or(company.companyNumber.containsIgnoreCase(searchKeyword))
                                        .or(company.ownerName.containsIgnoreCase(searchKeyword));

                        return baseClause != null ? baseClause.and(searchClause) : searchClause;
                }

                return baseClause;
        }
}
