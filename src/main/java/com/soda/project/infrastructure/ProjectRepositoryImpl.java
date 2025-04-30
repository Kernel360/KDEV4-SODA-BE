package com.soda.project.infrastructure;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateTimeExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.soda.project.domain.QProject;
import com.soda.project.domain.company.QCompanyProject;
import com.soda.project.domain.enums.ProjectStatus;
import com.soda.project.domain.member.QMemberProject;
import com.soda.project.domain.stage.QStage;
import com.soda.project.domain.stage.article.QArticle;
import com.soda.project.domain.stage.request.QRequest;
import com.soda.project.interfaces.dto.ProjectListResponse;
import com.soda.project.interfaces.dto.ProjectSearchCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;


@Repository
@RequiredArgsConstructor
public class ProjectRepositoryImpl implements ProjectRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private static final QProject project = QProject.project;
    private static final QMemberProject memberProject = QMemberProject.memberProject;
    private static final QCompanyProject companyProject = QCompanyProject.companyProject;

    @Override
    public Page<Tuple> findMyProjectsData(ProjectSearchCondition projectSearchCondition, Long memberId, Pageable pageable) {
        List<Tuple> content = queryFactory
                .select(
                        project,
                        companyProject.companyProjectRole,
                        memberProject.role
                )
                .from(project)
                // 1. 특정 멤버가 참여하는 프로젝트 필터링
                .join(project.memberProjects, memberProject)
                .join(project.companyProjects, companyProject)
                .on(companyProject.company.id.eq(
                        memberProject.member.company.id
                ))
                .where(
                        memberProject.member.id.eq(memberId),
                        project.isDeleted.isFalse(),
                        memberProject.isDeleted.isFalse(),
                        companyProject.isDeleted.isFalse(), // 회사 연결도 활성 상태
                        statusEq(projectSearchCondition.getStatus()),
                        titleContains(projectSearchCondition.getKeyword())
                )
                .orderBy(project.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // Count 쿼리도 동일한 조인 및 조건 적용
        JPAQuery<Long> countQuery = queryFactory
                .select(project.countDistinct())
                .from(project)
                .join(project.memberProjects, memberProject)
                .join(project.companyProjects, companyProject)
                .on(companyProject.company.id.eq(memberProject.member.company.id))
                .where(
                        memberProject.member.id.eq(memberId),
                        project.isDeleted.isFalse(),
                        memberProject.isDeleted.isFalse(),
                        companyProject.isDeleted.isFalse(),
                        statusEq(projectSearchCondition.getStatus()),
                        titleContains(projectSearchCondition.getKeyword())
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<Tuple> findMyCompanyProjectsData(Long memberId, Long companyId, Pageable pageable) {
        List<Tuple> content = queryFactory
                .select(
                        project,
                        companyProject.companyProjectRole,
                        memberProject.role
                )
                .from(project)
                // 회사가 참여한 프로젝트 찾기
                .join(project.companyProjects, companyProject)
                // 현재 사용자의 멤버 역할 찾기
                .leftJoin(project.memberProjects, memberProject)
                .on(memberProject.member.id.eq(memberId) // 현재 사용자
                        .and(memberProject.isDeleted.isFalse()))
                .where(
                        companyProject.company.id.eq(companyId),
                        project.isDeleted.isFalse(),
                        companyProject.isDeleted.isFalse()
                )
                .orderBy(project.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // Count 쿼리 (회사 기준으로 count)
        JPAQuery<Long> countQuery = queryFactory
                .select(project.countDistinct())
                .from(project)
                .join(project.companyProjects, companyProject)
                .where(
                        companyProject.company.id.eq(companyId),
                        project.isDeleted.isFalse(),
                        companyProject.isDeleted.isFalse()
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<ProjectListResponse> searchProjects(ProjectSearchCondition condition, Pageable pageable) {
        QProject project = QProject.project;
        QStage stage = QStage.stage;
        QRequest requestEntity = QRequest.request;
        QArticle article = QArticle.article;

        LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);

        BooleanBuilder where = new BooleanBuilder()
                .and(project.isDeleted.isFalse())
                .and(statusEq(condition.getStatus()))
                .and(titleContains(condition.getKeyword()));

        boolean sortByWeeklyActivity = pageable.getSort().stream()
                .anyMatch(order -> order.getProperty().equals("weeklyActivity"));

        NumberExpression<Long> weeklyRequestCount = Expressions.numberTemplate(Long.class, "({0})",
                JPAExpressions.select(requestEntity.count())
                        .from(requestEntity)
                        .join(requestEntity.stage, stage)
                        .where(stage.project.eq(project), requestEntity.createdAt.goe(oneWeekAgo))
        );

        NumberExpression<Long> weeklyArticleCount = Expressions.numberTemplate(Long.class, "({0})",
                JPAExpressions.select(article.count())
                        .from(article)
                        .join(article.stage, stage)
                        .where(stage.project.eq(project), article.createdAt.goe(oneWeekAgo))
        );

        NumberExpression<Long> weeklyActivity = weeklyRequestCount.add(weeklyArticleCount);

        DateTimeExpression<LocalDateTime> recentRequestDate = Expressions.dateTimeTemplate(
                LocalDateTime.class, "({0})",
                JPAExpressions.select(requestEntity.createdAt.max())
                        .from(requestEntity)
                        .join(requestEntity.stage, stage)
                        .where(stage.project.eq(project))
        );

        DateTimeExpression<LocalDateTime> recentArticleDate = Expressions.dateTimeTemplate(
                LocalDateTime.class, "({0})",
                JPAExpressions.select(article.createdAt.max())
                        .from(article)
                        .join(article.stage, stage)
                        .where(stage.project.eq(project))
        );

        JPQLQuery<ProjectListResponse> query = queryFactory
                .select(Projections.constructor(ProjectListResponse.class,
                        project.id,
                        project.title,
                        project.status,
                        project.startDate,
                        project.endDate,
                        weeklyRequestCount,
                        weeklyArticleCount,
                        weeklyActivity,
                        recentRequestDate,
                        recentArticleDate
                ))
                .from(project)
                .where(where);

        if (sortByWeeklyActivity) {
            query.orderBy(weeklyActivity.desc());
        } else {
            query.orderBy(project.createdAt.desc());
        }

        List<ProjectListResponse> content = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long count = queryFactory
                .select(project.count())
                .from(project)
                .where(where)
                .fetchOne();

        return new PageImpl<>(content, pageable, count);
    }




    private BooleanExpression statusEq(ProjectStatus status) {
        return status != null ? project.status.eq(status) : null;
    }

    private BooleanExpression titleContains(String keyword) {
        // StringUtils.hasText 사용하여 null 또는 빈 문자열 체크
        return StringUtils.hasText(keyword) ? project.title.containsIgnoreCase(keyword) : null;
    }
}