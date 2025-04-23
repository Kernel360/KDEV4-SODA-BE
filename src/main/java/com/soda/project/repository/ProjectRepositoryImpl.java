package com.soda.project.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.soda.project.dto.ProjectSearchCondition;
import com.soda.project.entity.Project;
import com.soda.project.entity.QCompanyProject;
import com.soda.project.entity.QMemberProject;
import com.soda.project.entity.QProject;
import com.soda.project.enums.ProjectStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;


@Repository
@RequiredArgsConstructor
public class ProjectRepositoryImpl implements ProjectRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private static final QProject project = QProject.project;
    private static final QMemberProject memberProject = QMemberProject.memberProject;
    private static final QCompanyProject companyProject = QCompanyProject.companyProject;

    @Override
    public Page<Tuple> findMyProjectsData(Long memberId, Pageable pageable) {
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
                        companyProject.isDeleted.isFalse() // 회사 연결도 활성 상태
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
                        companyProject.isDeleted.isFalse()
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
    public Page<Project> searchProjects(ProjectSearchCondition request, Pageable pageable) {
        // 데이터 조회 쿼리
        List<Project> content = queryFactory
                .selectFrom(project)
                .where(
                        project.isDeleted.isFalse(),
                        statusEq(request.getStatus()),
                        titleContains(request.getKeyword())
                )
                .orderBy(project.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // Count 쿼리 (동일한 조건 사용)
        JPAQuery<Long> countQuery = queryFactory
                .select(project.count())
                .from(project)
                .where(
                        project.isDeleted.isFalse(),
                        statusEq(request.getStatus()),
                        titleContains(request.getKeyword())
                );

        // Page 객체 생성 및 반환
        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression statusEq(ProjectStatus status) {
        return status != null ? project.status.eq(status) : null;
    }

    private BooleanExpression titleContains(String keyword) {
        // StringUtils.hasText 사용하여 null 또는 빈 문자열 체크
        return StringUtils.hasText(keyword) ? project.title.containsIgnoreCase(keyword) : null;
    }
}