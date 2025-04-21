package com.soda.project.repository;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.soda.project.entity.QCompanyProject;
import com.soda.project.entity.QMemberProject;
import com.soda.project.entity.QProject;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

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
                        project,             // Project 엔티티
                        companyProject.companyProjectRole, // 회사 역할
                        memberProject.role   // 멤버 역할
                )
                .from(project)
                // 1. MemberProject와 조인하여 특정 멤버가 참여하는 프로젝트 필터링
                .join(project.memberProjects, memberProject)
                // 2. CompanyProject와 조인 (별도의 ON 조건 필요)
                .join(project.companyProjects, companyProject)
                // ON 조건: CompanyProject의 회사 ID가 MemberProject의 멤버가 속한 회사 ID와 같아야 함
                .on(companyProject.company.id.eq(
                        // 서브쿼리 대신 MemberProject의 member를 통해 company ID를 가져옴
                        memberProject.member.company.id
                ))
                .where(
                        // memberProject의 member ID가 주어진 memberId와 일치해야 함
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
        // (이전 답변과 동일 - LEFT JOIN 로직 유효)
        List<Tuple> content = queryFactory
                .select(
                        project,             // Project 엔티티 전체
                        companyProject.companyProjectRole, // 회사 역할
                        memberProject.role   // 멤버 역할 (null 가능)
                )
                .from(project)
                // 회사가 참여한 프로젝트 찾기 (INNER JOIN)
                .join(project.companyProjects, companyProject)
                // 현재 사용자의 멤버 역할 찾기 (LEFT JOIN)
                .leftJoin(project.memberProjects, memberProject)
                .on(memberProject.member.id.eq(memberId) // 현재 사용자이고
                        .and(memberProject.isDeleted.isFalse())) // 삭제되지 않은 연결
                .where(
                        companyProject.company.id.eq(companyId), // 특정 회사 필터
                        project.isDeleted.isFalse(),
                        companyProject.isDeleted.isFalse() // 회사 연결 활성
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
}