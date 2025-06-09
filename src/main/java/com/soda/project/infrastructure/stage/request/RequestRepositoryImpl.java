package com.soda.project.infrastructure.stage.request;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.PathBuilder;
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

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.soda.member.domain.member.QMember.member;
import static com.soda.project.domain.QProject.project;
import static com.soda.project.domain.stage.QStage.stage;


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
                .join(request.stage, stage).fetchJoin()
                .join(stage.project, project).fetchJoin()
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
        BooleanBuilder baseCondition = buildCommonCondition(condition);

        List<Long> requesterIds = findRequestIdsByRequester(memberId, baseCondition);
        List<Long> approverIds = findRequestIdsByApprover(memberId, baseCondition);

        List<Long> sortedMergedIds = mergeAndSortIds(requesterIds, approverIds);
        List<Long> pagedIds = paginate(sortedMergedIds, pageable);

        List<RequestDTO> unorderedContent = fetchRequestsByIds(pagedIds);
        List<RequestDTO> orderedContent = restoreOrder(unorderedContent, pagedIds);

        return new PageImpl<>(orderedContent, pageable, sortedMergedIds.size());
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

    private BooleanBuilder buildCommonCondition(GetMemberRequestCondition condition) {
        QRequest request = QRequest.request;
        QProject project = QProject.project;

        BooleanBuilder builder = new BooleanBuilder();
        if (condition.getProjectId() != null) {
            builder.and(project.id.eq(condition.getProjectId()));
        }
        if (condition.getKeyword() != null && !condition.getKeyword().isBlank()) {
            builder.and(request.title.containsIgnoreCase(condition.getKeyword()));
        }
        builder.and(project.isDeleted.eq(false));

        return builder;
    }

    private List<Long> findRequestIdsByRequester(Long memberId, BooleanBuilder condition) {
        QRequest request = QRequest.request;
        QStage stage = QStage.stage;
        QProject project = QProject.project;

        return queryFactory
                .select(request.id)
                .from(request)
                .join(request.stage, stage)
                .join(stage.project, project)
                .where(new BooleanBuilder(condition).and(request.member.id.eq(memberId)))
                .fetch();
    }

    private List<Long> findRequestIdsByApprover(Long memberId, BooleanBuilder condition) {
        QRequest request = QRequest.request;
        QStage stage = QStage.stage;
        QProject project = QProject.project;
        QApproverDesignation approver = QApproverDesignation.approverDesignation;

        return queryFactory
                .select(request.id)
                .from(request)
                .join(request.stage, stage)
                .join(stage.project, project)
                .join(request.approvers, approver)
                .where(new BooleanBuilder(condition).and(approver.member.id.eq(memberId)))
                .fetch();
    }

    private List<Long> mergeAndSortIds(List<Long> list1, List<Long> list2) {
        return Stream.concat(list1.stream(), list2.stream())
                .distinct()
                .sorted(Comparator.reverseOrder()) // 최신순
                .toList();
    }

    private List<Long> paginate(List<Long> ids, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), ids.size());
        return (start >= ids.size()) ? Collections.emptyList() : ids.subList(start, end);
    }

    private List<RequestDTO> fetchRequestsByIds(List<Long> ids) {
        if (ids.isEmpty()) return Collections.emptyList();

        QRequest request = QRequest.request;
        QStage stage = QStage.stage;
        QProject project = QProject.project;
        QMember member = QMember.member;

        return queryFactory
                .select(new QRequestDTO(
                        request.id,
                        project.id,
                        stage.id,
                        request.member.id,
                        request.member.name,
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
                .where(request.id.in(ids))
                .fetch();
    }

    private List<RequestDTO> restoreOrder(List<RequestDTO> unordered, List<Long> orderedIds) {
        Map<Long, Integer> idOrderMap = new HashMap<>();
        for (int i = 0; i < orderedIds.size(); i++) {
            idOrderMap.put(orderedIds.get(i), i);
        }

        return unordered.stream()
                .sorted(Comparator.comparingInt(dto -> idOrderMap.get(dto.getRequestId())))
                .toList();
    }


}
