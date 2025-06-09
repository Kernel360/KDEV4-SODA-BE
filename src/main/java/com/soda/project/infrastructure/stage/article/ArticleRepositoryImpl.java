package com.soda.project.infrastructure.stage.article;

import static com.soda.member.domain.company.QCompany.company;
import static com.soda.member.domain.member.QMember.member;
import static com.soda.project.domain.QProject.project;
import static com.soda.project.domain.stage.QStage.stage;
import static com.soda.project.domain.stage.article.QArticle.article;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.soda.project.domain.stage.article.Article;
import com.soda.project.domain.stage.article.enums.ArticleStatus;
import com.soda.project.domain.stage.article.enums.PriorityType;
import com.soda.project.interfaces.stage.article.dto.ArticleSearchCondition;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
@RequiredArgsConstructor
public class ArticleRepositoryImpl implements ArticleRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Tuple> findMyArticlesData(Long authorId, Long projectId, Pageable pageable) {
        List<Tuple> content = executeContentQuery(authorId, projectId, pageable);
        Long totalCount = executeCountQuery(authorId, projectId);
        return PageableExecutionUtils.getPage(content, pageable, () -> totalCount);
    }

    // Content Query 로직 분리
    private List<Tuple> executeContentQuery(Long authorId, Long projectId, Pageable pageable) {
        return queryFactory
            .select(
                article.id,
                article.title,
                project.id,
                project.title,
                stage.id,
                stage.name,
                article.createdAt
            )
            .from(article)
            .join(article.stage, stage)
            .join(stage.project, project)
            .where(
                article.member.id.eq(authorId),
                article.isDeleted.isFalse(),
                projectIdEq(projectId) // projectId가 null이면 이 조건은 무시
            )
            .orderBy(article.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();
    }

    // Count Query 로직 분리
    private Long executeCountQuery(Long authorId, Long projectId) {
        JPAQuery<Long> countQuery = queryFactory
            .select(article.count())
            .from(article);

        // projectId가 유효할 때만 stage, project 조인하고, where 조건에 추가
        if (projectId != null && projectId > 0) {
            countQuery.join(article.stage, stage)
                .join(stage.project, project)
                .where(project.id.eq(projectId));
        }

        // 공통 where 조건 추가
        countQuery.where(
            article.member.id.eq(authorId),
            article.isDeleted.isFalse()
        );

        Long total = countQuery.fetchOne();
        return (total == null) ? 0L : total;
    }

    @Override
    public Optional<Article> findByIdAndIsDeletedFalseWithMemberAndCompanyUsingQuerydsl(Long articleId) {
        Article foundArticle = queryFactory
                .selectFrom(article)
                .leftJoin(article.member, member).fetchJoin()
                .leftJoin(member.company, company).fetchJoin()
                .where(
                        article.id.eq(articleId),
                        article.isDeleted.isFalse()
                )
                .fetchOne(); // 단 건 조회

        // 조회 결과가 null일 수 있으므로 Optional 로 감싸서 반환
        return Optional.ofNullable(foundArticle);
    }

    @Override
    public Page<Article> searchArticles(Long projectId, ArticleSearchCondition request, Pageable pageable) {
        List<Article> content = queryFactory
                .selectFrom(article)
                .leftJoin(article.stage, stage).fetchJoin()    // Fetch Join 유지 또는 필요시 제거/변경
                .leftJoin(article.member, member).fetchJoin()    // Fetch Join 유지 또는 필요시 제거/변경
                .leftJoin(member.company, company).fetchJoin() // Company 정보도 필요하면 Fetch Join
                .where(
                        stage.project.id.eq(projectId),
                        article.isDeleted.isFalse(),
                        stageIdEq(request.getStageId()),
                        searchCondition(request.getSearchType(), request.getKeyword()),
                        articleStatusEq(request.getStatus()),
                        priorityTypeEq(request.getPriorityType())
                )
                .orderBy(article.createdAt.desc()) // 기본 정렬, Pageable 정렬 처리 필요시 추가
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // Count 쿼리
        JPAQuery<Long> countQuery = queryFactory
                .select(article.count())
                .from(article)
                .join(article.stage, stage) // where 조건에서 stage 사용하므로 필요
                .join(article.member, member) // where 조건에서 member 사용하면 필요 (searchCondition 확인)
                .where(
                        stage.project.id.eq(projectId),
                        article.isDeleted.isFalse(),
                        stageIdEq(request.getStageId()),
                        searchCondition(request.getSearchType(), request.getKeyword())
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression priorityTypeEq(PriorityType priorityType) {
        return priorityType != null ? article.priority.eq(priorityType) : null;
    }

    private BooleanExpression articleStatusEq(ArticleStatus status) {
        return status != null ? article.status.eq(status) : null;
    }

    // 프로젝트 ID 필터링 조건 (project 별칭 사용)
    private BooleanExpression projectIdEq(Long projectId) {
        // projectId가 null이 아니고 유효한 값일 때만 project.id 와 비교
        return (projectId != null && projectId > 0) ? project.id.eq(projectId) : null;
    }

    private BooleanExpression stageIdEq(Long stageId) {
        return stageId != null ? stage.id.eq(stageId) : null;
    }

    private BooleanExpression searchCondition(ArticleSearchCondition.SearchType searchType, String keyword) {
        if (!StringUtils.hasText(keyword) || searchType == null) {
            return null; // 키워드나 타입 없으면 조건 없음
        }

        return switch (searchType) {
            case TITLE_CONTENT -> article.title.containsIgnoreCase(keyword)
                    .or(article.content.containsIgnoreCase(keyword)); // 제목 또는 내용
            case AUTHOR -> member.name.containsIgnoreCase(keyword); // 작성자 이름 (member 조인 필요)
        };
    }

}