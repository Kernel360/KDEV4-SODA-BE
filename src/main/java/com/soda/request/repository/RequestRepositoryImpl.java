package com.soda.request.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.soda.request.dto.GetRequestCondition;
import com.soda.request.dto.request.GetMemberRequestCondition;
import com.soda.request.entity.QApproverDesignation;
import com.soda.request.entity.QRequest;
import com.soda.request.entity.Request;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

import static com.soda.member.entity.QMember.member;

@Slf4j
@Repository
public class RequestRepositoryImpl implements RequestRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public RequestRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Page<Request> searchByCondition(GetRequestCondition condition, Pageable pageable) {
        QRequest request = QRequest.request;
        BooleanBuilder builder = new BooleanBuilder();

        if (condition.getStageId() != null) {
            builder.and(request.stage.id.eq(condition.getStageId()));
        }
        if (condition.getStatus() != null) {
            builder.and(request.status.eq(condition.getStatus()));
        }
        if (condition.getKeyword() != null && !condition.getKeyword().isBlank()) {
            builder.and(
                    request.title.containsIgnoreCase(condition.getKeyword())
                            .or(request.member.name.containsIgnoreCase(condition.getKeyword()))
            );
        }

        List<OrderSpecifier<?>> orderSpecifiers = getOrderSpecifiers(pageable.getSort(), request);

        JPQLQuery<Request> query = queryFactory
                .selectFrom(request)
                .join(request.member, member).fetchJoin()
                .where(builder.and(request.isDeleted.eq(false)))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        if (!orderSpecifiers.isEmpty()) {
            query.orderBy(orderSpecifiers.toArray(new OrderSpecifier[0]));
        } else {
            query.orderBy(request.createdAt.desc());
        }

        List<Request> content = query.fetch();

        long total = queryFactory
                .selectFrom(request)
                .join(request.member, member)
                .where(builder.and(request.isDeleted.eq(false)))
                .fetchCount();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<Request> searchByMemberCondition(Long memberId, GetMemberRequestCondition condition, Pageable pageable) {
        QRequest request = QRequest.request;
        QApproverDesignation approverDesignation = QApproverDesignation.approverDesignation;

        BooleanBuilder baseCondition = new BooleanBuilder();

        if (condition.getProjectId() != null) {
            baseCondition.and(request.stage.project.id.eq(condition.getProjectId()));
        }

        BooleanExpression requesterCondition = request.member.id.eq(memberId);

        JPQLQuery<Request> query = queryFactory
                .selectFrom(request)
                .leftJoin(request.approvers, approverDesignation).fetchJoin()
                .where(baseCondition.and(
                        requesterCondition.or(approverDesignation.member.id.eq(memberId))
                ))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        List<OrderSpecifier<?>> orderSpecifiers = getOrderSpecifiers(pageable.getSort(), request);
        if (!orderSpecifiers.isEmpty()) {
            query.orderBy(orderSpecifiers.toArray(new OrderSpecifier[0]));
        } else {
            query.orderBy(request.createdAt.desc());
        }

        List<Request> content = query.fetch();

        long total = queryFactory
                .selectFrom(request)
                .leftJoin(request.approvers, approverDesignation)
                .where(baseCondition.and(
                        requesterCondition.or(approverDesignation.member.id.eq(memberId))
                ))
                .fetchCount();

        return new PageImpl<>(content, pageable, total);
    }


    private List<OrderSpecifier<?>> getOrderSpecifiers(Sort sort, QRequest request) {
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();

        for (Sort.Order order : sort) {
            PathBuilder<Request> pathBuilder = new PathBuilder<>(Request.class, "request");
            switch (order.getProperty()) {
                case "createdAt":
                    orderSpecifiers.add(order.isAscending() ? request.createdAt.asc() : request.createdAt.desc());
                    break;
                case "title":
                    orderSpecifiers.add(order.isAscending() ? request.title.asc() : request.title.desc());
                    break;
                case "status":
                    orderSpecifiers.add(order.isAscending() ? request.status.asc() : request.status.desc());
                    break;
                case "stage":
                    orderSpecifiers.add(order.isAscending() ? request.stage.id.asc() : request.stage.id.desc());
                    break;
                default:
                    log.info("Unknown order property in Request Sort Paigination {}", order.getProperty());
                    break;
            }
        }

        return orderSpecifiers;
    }

}
