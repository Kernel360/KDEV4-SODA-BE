package com.soda.request.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.soda.request.dto.GetRequestCondition;
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

        List<OrderSpecifier<?>> orderSpecifiers = getOrderSpecifiers(pageable.getSort(), request);

        JPQLQuery<Request> query = queryFactory
                .selectFrom(request)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        if (!orderSpecifiers.isEmpty()) {
            query.orderBy(orderSpecifiers.toArray(new OrderSpecifier[0]));
        } else {
            query.orderBy(request.createdAt.desc()); // 기본 정렬
        }

        List<Request> content = query.fetch();

        long total = queryFactory
                .selectFrom(request)
                .where(builder)
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
