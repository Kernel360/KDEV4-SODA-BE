package com.soda.project.infrastructure.stage.request;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.soda.member.domain.member.QMember;
import com.soda.project.domain.QProject;
import com.soda.project.domain.stage.QStage;
import com.soda.project.domain.stage.request.QRequest;
import com.soda.project.domain.stage.request.Request;
import com.soda.project.domain.stage.request.approver.QApproverDesignation;
import com.soda.project.interfaces.stage.request.dto.GetMemberRequestCondition;
import com.soda.project.interfaces.stage.request.dto.GetRequestCondition;
import com.soda.project.interfaces.stage.request.dto.QRequestDTO;
import com.soda.project.interfaces.stage.request.dto.RequestDTO;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.soda.member.domain.member.QMember.member;


@Slf4j
@Repository
public class RequestRepositoryImpl implements RequestRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public RequestRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Page<Request> searchByCondition(Long projectId, GetRequestCondition condition, Pageable pageable) {
        QRequest request = QRequest.request;
        BooleanBuilder builder = new BooleanBuilder();

        builder.and(request.stage.project.id.eq(projectId));
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

        List<Request> allRequests = queryFactory
                .selectFrom(request)
                .join(request.member, member).fetchJoin()
                .where(builder.and(request.isDeleted.eq(false)))
                .orderBy(request.createdAt.desc())
                .fetch();

        List<Request> rootRequests = allRequests.stream()
                .filter(r -> r.getParentId() == null)
                .collect(Collectors.toList());

        Map<Long, List<Request>> childRequestMap = allRequests.stream()
                .filter(r -> r.getParentId() != null)
                .collect(Collectors.groupingBy(Request::getParentId));

        List<Request> sortedRequests = new ArrayList<>();
        for (Request root : rootRequests) {
            sortedRequests.add(root);
            List<Request> children = childRequestMap.getOrDefault(root.getId(), new ArrayList<>());
            children.sort(Comparator.comparing(Request::getCreatedAt).reversed()); // 자식도 최신순
            sortedRequests.addAll(children);
        }

        int fromIndex = (int) pageable.getOffset();
        int toIndex = Math.min(fromIndex + pageable.getPageSize(), sortedRequests.size());
        List<Request> pagedRequests = sortedRequests.subList(fromIndex, toIndex);

        return new PageImpl<>(pagedRequests, pageable, sortedRequests.size());
    }

    @Override
    public Page<RequestDTO> searchDtosByMemberCondition(Long memberId, GetMemberRequestCondition condition, Pageable pageable) {
        QRequest request = QRequest.request;
        QMember member = QMember.member;
        QStage stage = QStage.stage;
        QProject project = QProject.project;
        QApproverDesignation approver = QApproverDesignation.approverDesignation;

        BooleanBuilder baseCondition = new BooleanBuilder();

        if (condition.getProjectId() != null) {
            baseCondition.and(project.id.eq(condition.getProjectId()));
        }
        if (condition.getKeyword() != null && !condition.getKeyword().isBlank()) {
            baseCondition.and(request.title.containsIgnoreCase(condition.getKeyword()));
        }
        baseCondition.and(project.isDeleted.eq(false));

        BooleanExpression requesterCondition = request.member.id.eq(memberId);

        JPQLQuery<RequestDTO> query = queryFactory
                .select(new QRequestDTO(
                        request.id,
                        project.id,
                        stage.id,
                        member.id,
                        member.name,
                        request.parentId,
                        request.title,
                        request.content,
                        request.status,
                        request.createdAt,
                        request.updatedAt
                ))
                .from(request)
                .join(request.member, member)
                .join(request.stage, stage)
                .join(stage.project, project)
                .leftJoin(request.approvers, approver)
                .where(baseCondition.and(
                        requesterCondition.or(approver.member.id.eq(memberId))
                ))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        List<OrderSpecifier<?>> orderSpecifiers = getOrderSpecifiers(pageable.getSort(), request);
        if (!orderSpecifiers.isEmpty()) {
            query.orderBy(orderSpecifiers.toArray(new OrderSpecifier[0]));
        } else {
            query.orderBy(request.createdAt.desc());
        }

        List<RequestDTO> content = query.fetch();

        long total = queryFactory
                .selectFrom(request)
                .leftJoin(request.approvers, approver)
                .join(request.stage, stage)
                .join(stage.project, project)
                .where(baseCondition.and(
                        requesterCondition.or(approver.member.id.eq(memberId))
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
