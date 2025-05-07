package com.soda.project.infrastructure.member;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.soda.project.domain.member.MemberProject;
import com.soda.project.domain.member.MemberProjectRole;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.List;

import static com.soda.member.domain.QMember.member;
import static com.soda.member.domain.company.QCompany.company;
import static com.soda.project.domain.member.QMemberProject.memberProject;

@Repository
@RequiredArgsConstructor
public class MemberProjectRepositoryImpl implements MemberProjectRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<MemberProject> findFilteredMembers(Long projectId,
                                                   List<Long> companyIds,
                                                   Long companyId,
                                                   MemberProjectRole memberRole,
                                                   Long memberId,
                                                   Pageable pageable) {

        // 1. 데이터 조회 쿼리 (fetch join 사용)
        JPAQuery<MemberProject> dataQuery = queryFactory
                .selectFrom(memberProject)
                .join(memberProject.member, member).fetchJoin()
                .join(member.company, company).fetchJoin()
                .where(
                        memberProject.project.id.eq(projectId),
                        memberProject.isDeleted.isFalse(),
                        companyIdsIn(companyIds),
                        companyIdEq(companyId),
                        memberRoleEq(memberRole),
                        memberIdEq(memberId)
                )
                .orderBy(getOrderSpecifiers(pageable.getSort()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        List<MemberProject> content = dataQuery.fetch();

        // 2. Count 쿼리
        JPAQuery<Long> countQuery = queryFactory
                .select(memberProject.count())
                .from(memberProject)
                .join(memberProject.member, member)
                .join(member.company, company)
                .where(
                        memberProject.project.id.eq(projectId),
                        memberProject.isDeleted.isFalse(),
                        companyIdsIn(companyIds),
                        companyIdEq(companyId),
                        memberRoleEq(memberRole),
                        memberIdEq(memberId)
                );

        // 3. Page 객체 생성 및 반환
        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression memberIdEq(Long memberId) {
        return ObjectUtils.isEmpty(memberId) ? null : member.id.eq(memberId);
    }

    /**
     * 회사 ID 목록(companyIds) 조건을 생성합니다. 목록이 비어있으면 null을 반환합니다.
     */
    private BooleanExpression companyIdsIn(List<Long> companyIds) {
        return CollectionUtils.isEmpty(companyIds) ? null : company.id.in(companyIds);
    }

    /**
     * 개별 회사 ID(companyId) 조건을 생성합니다. ID가 null이면 null을 반환합니다.
     */
    private BooleanExpression companyIdEq(Long companyId) {
        return ObjectUtils.isEmpty(companyId) ? null : company.id.eq(companyId);
    }

    /**
     * 멤버 역할(memberRole) 조건을 생성합니다. 역할이 null이면 null을 반환
     */
    private BooleanExpression memberRoleEq(MemberProjectRole memberRole) {
        return ObjectUtils.isEmpty(memberRole) ? null : memberProject.role.eq(memberRole);
    }

    /**
     * Spring Data의 Sort 객체를 QueryDSL의 OrderSpecifier 배열로 변환합니다.
     *
     * @param sort Pageable에서 추출한 Sort 객체
     * @return QueryDSL의 orderBy 절에 사용할 OrderSpecifier 배열
     */
    private OrderSpecifier<?>[] getOrderSpecifiers(Sort sort) {
        if (sort == null || sort.isUnsorted()) {
            return new OrderSpecifier[]{new OrderSpecifier<>(Order.ASC, member.name)};
        }

        // PathBuilder를 사용하여 문자열 기반 속성 경로를 QueryDSL Path 객체로 변환
        PathBuilder<MemberProject> entityPath = new PathBuilder<>(MemberProject.class, "memberProject");

        return sort.stream()
                .map(order -> {
                    Order direction = order.isAscending() ? Order.ASC : Order.DESC;
                    String property = order.getProperty();

                    PathBuilder<?> path = resolvePath(entityPath, property);

                    return new OrderSpecifier(direction, path);
                })
                .toArray(OrderSpecifier[]::new);
    }

    /**
     * 점(.)으로 구분된 속성 문자열(예: "member.name")을 기반으로 실제 PathBuilder 경로를 찾습니다.
     *
     * @param rootPath 시작 PathBuilder (엔티티 루트)
     * @param property 속성 문자열
     * @return 해당 속성에 대한 PathBuilder
     */
    private PathBuilder<?> resolvePath(PathBuilder<?> rootPath, String property) {
        PathBuilder<?> currentPath = rootPath;
        String[] parts = property.split("\\.");
        for (String part : parts) {
            currentPath = currentPath.get(part, Comparable.class);
        }
        return currentPath;
    }
}