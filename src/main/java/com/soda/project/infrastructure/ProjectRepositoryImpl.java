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
import com.soda.member.domain.company.QCompany;
import com.soda.member.domain.member.QMember;
import com.soda.project.domain.Project;
import com.soda.project.domain.ProjectStatus;
import com.soda.project.domain.QProject;
import com.soda.project.domain.company.CompanyProjectRole;
import com.soda.project.domain.company.QCompanyProject;
import com.soda.project.domain.member.MemberProjectRole;
import com.soda.project.domain.member.QMemberProject;
import com.soda.project.domain.stage.QStage;
import com.soda.project.domain.stage.article.QArticle;
import com.soda.project.domain.stage.request.QRequest;
import com.soda.project.interfaces.dto.MyProjectListResponse;
import com.soda.project.interfaces.dto.ProjectListResponse;
import com.soda.project.interfaces.dto.ProjectSearchCondition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ProjectRepositoryImpl implements ProjectRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private static final QProject project = QProject.project;
    private static final QMemberProject memberProject = QMemberProject.memberProject;
    private static final QCompanyProject companyProject = QCompanyProject.companyProject;
    private static final QMember member = QMember.member;
    private static final QCompany company = QCompany.company;

    @Override
    public Page<MyProjectListResponse> findMyProjectsData(ProjectSearchCondition projectSearchCondition, Long memberId, Pageable pageable) {
        List<Long> projectIds = queryFactory
                .select(project.id)
                .from(project)
                .join(project.memberProjects, memberProject)
                .where(
                        project.isDeleted.isFalse(),
                        memberProject.member.id.eq(memberId),
                        memberProject.isDeleted.isFalse(),
                        statusEq(projectSearchCondition.getStatus()),
                        titleContains(projectSearchCondition.getKeyword())
                )
                .orderBy(project.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        if (projectIds.isEmpty()) {
            return Page.empty(pageable);
        }

        List<Tuple> tuples = queryFactory
                .select(
                        project,
                        companyProject.companyProjectRole,
                        memberProject.role
                )
                .from(project)
                .join(project.memberProjects, memberProject)
                .on(memberProject.project.id.in(projectIds)
                        .and(memberProject.member.id.eq(memberId))
                        .and(memberProject.isDeleted.isFalse()))
                .join(memberProject.member, member)
                .leftJoin(project.companyProjects, companyProject)
                .on(companyProject.project.eq(project)
                        .and(companyProject.company.id.eq(member.company.id))
                        .and(companyProject.isDeleted.isFalse()))
                .fetch();

        Map<Long, Tuple> tupleMap = tuples.stream()
                .collect(Collectors.toMap(t -> t.get(project).getId(), Function.identity(), (t1, t2) -> t1));

        List<MyProjectListResponse> content = projectIds.stream()
                .map(pId -> {
                    Tuple tuple = tupleMap.get(pId);
                    if (tuple == null) {
                        log.warn("Tuple not found for projectId: {} in tupleMap for /projects/my. This might happen if there's data inconsistency.", pId);
                        return null;
                    }
                    Project p = tuple.get(project);
                    CompanyProjectRole cpr = tuple.get(companyProject.companyProjectRole);
                    MemberProjectRole mpr = tuple.get(memberProject.role);
                    if (p == null || mpr == null) {
                        log.error("Critical: Project or MemberProjectRole is null in tuple for projectId {} and memberId {}.", (p != null ? p.getId() : "null_project"), memberId);
                        return null;
                    }
                    return MyProjectListResponse.from(p, cpr, mpr);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        JPAQuery<Long> countQuery = queryFactory
                .select(project.count())
                .from(project)
                .join(project.memberProjects, memberProject)
                .where(
                        project.isDeleted.isFalse(),
                        memberProject.member.id.eq(memberId),
                        memberProject.isDeleted.isFalse(),
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
                .join(project.companyProjects, companyProject)
                .leftJoin(project.memberProjects, memberProject)
                .on(memberProject.member.id.eq(memberId)
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
        return StringUtils.hasText(keyword) ? project.title.containsIgnoreCase(keyword) : null;
    }
}