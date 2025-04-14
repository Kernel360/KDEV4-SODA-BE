package com.soda.request.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.soda.request.dto.GetRequestCondition;
import com.soda.request.entity.QRequest;
import com.soda.request.entity.Request;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

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

        List<Request> content = queryFactory
                .selectFrom(request)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(request.createdAt.desc()) // 기본 정렬 지정
                .fetch();

        long total = queryFactory
                .selectFrom(request)
                .where(builder)
                .fetchCount();

        return new PageImpl<>(content, pageable, total);
    }
}
