package com.soda.article.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.soda.article.entity.QArticle.article;
import static com.soda.project.entity.QProject.project;
import static com.soda.project.entity.QStage.stage;

@Repository
@RequiredArgsConstructor
public class ArticleRepositoryImpl implements ArticleRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Tuple> findMyArticlesData(Long authorId, Long projectId, Pageable pageable) {

        List<Tuple> content = queryFactory
                .select( // DTO 필드 순서에 맞춰 데이터 선택
                        article.id,        // articleId
                        article.title,     // title
                        project.id,        // projectId (stage를 통해 조인)
                        project.title,     // projectName (stage를 통해 조인)
                        stage.id,          // stageId
                        stage.name,   // stageName
                        article.createdAt  // createdAt
                )
                .from(article)
                // Article -> Stage 조인
                .join(article.stage, stage)
                // Stage -> Project 조인 (Stage 엔티티에 'project' 필드가 있다고 가정)
                .join(stage.project, project)
                // Article -> Member 조인 (where 절에서만 사용하므로 명시적 조인 불필요 가능하나, 가독성을 위해 포함)
                // .join(article.member, member) // member Q 클래스 정의 필요
                .where(
                        // article.member 필드를 통해 바로 작성자 ID 비교
                        article.member.id.eq(authorId),
                        article.isDeleted.isFalse(),
                        // projectIdEq 헬퍼 메서드는 project 별칭을 사용
                        projectIdEq(projectId)
                )
                .orderBy(article.createdAt.desc()) // 최신순 정렬
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // Count 쿼리
        JPAQuery<Long> countQuery = queryFactory
                .select(article.count())
                .from(article)
                // where 조건에서 project.id를 사용하므로 조인이 필요함
                .join(article.stage, stage)
                .join(stage.project, project)
                // .join(article.member, member) // where 조건에서만 사용 시 count 쿼리 조인 불필요 가능
                .where(
                        article.member.id.eq(authorId),
                        article.isDeleted.isFalse(),
                        projectIdEq(projectId)
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    // 프로젝트 ID 필터링 조건 (project 별칭 사용)
    private BooleanExpression projectIdEq(Long projectId) {
        // projectId가 null이 아니고 유효한 값일 때만 project.id 와 비교
        return (projectId != null && projectId > 0) ? project.id.eq(projectId) : null;
    }
}