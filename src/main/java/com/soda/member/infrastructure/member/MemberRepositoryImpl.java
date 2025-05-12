package com.soda.member.infrastructure.member;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.soda.member.domain.member.Member;
import com.soda.member.domain.member.QMember;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom {
        private final JPAQueryFactory queryFactory;


        @Override
        public Page<Member> findByKeywordIncludingDeletedOrderByCreatedAtDesc(String keyword, Pageable pageable) {
                QMember member = QMember.member;
                BooleanExpression keywordCondition = createKeywordCondition(member, keyword);

                List<Member> members = queryFactory
                                .selectFrom(member)
                                .where(keywordCondition)
                                .orderBy(member.createdAt.desc())
                                .offset(pageable.getOffset())
                                .limit(pageable.getPageSize())
                                .fetch();

                Long total = queryFactory
                                .select(member.count())
                                .from(member)
                                .where(keywordCondition)
                                .fetchOne();

                return PageableExecutionUtils.getPage(members, pageable, () -> total);
        }

        private BooleanExpression createKeywordCondition(QMember member, String keyword) {
                if (!StringUtils.hasText(keyword)) {
                        return null;
                }
                return member.name.containsIgnoreCase(keyword)
                                .or(member.email.containsIgnoreCase(keyword))
                                .or(member.authId.containsIgnoreCase(keyword))
                        .or(member.company.name.containsIgnoreCase(keyword));
        }
}