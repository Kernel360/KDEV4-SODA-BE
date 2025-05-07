package com.soda.member.infrastructure;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.soda.member.domain.company.QCompany;
import com.soda.member.interfaces.dto.CompanyCreationStatRaw;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

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
                        company.count().as("count")
                ))
                .from(company)
                .where(
                        company.createdAt.goe(startDate),
                        company.createdAt.lt(endDate),
                        company.isDeleted.isFalse()
                )
                .groupBy(year, month, week, day)
                .orderBy(year.asc(), month.asc(), week.asc(), day.asc())
                .fetch();
    }
}
