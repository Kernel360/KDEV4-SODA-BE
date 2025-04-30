package com.soda.project.article.repository;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.soda.article.entity.QVoteAnswer.voteAnswer;
import static com.soda.article.entity.QVoteAnswerItem.voteAnswerItem;

@Repository
@RequiredArgsConstructor
public class VoteAnswerItemRepositoryImpl implements VoteAnswerItemRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public Map<Long, Long> countItemGroupByVoteItemId(Long voteId) {
        List<Tuple> results = queryFactory
                .select(
                        voteAnswerItem.voteItem.id,
                        voteAnswerItem.id.count()
                )
                .from(voteAnswerItem)
                .join(voteAnswerItem.voteResponse, voteAnswer)
                .where(
                        voteAnswer.vote.id.eq(voteId)
                )
                .groupBy(voteAnswerItem.voteItem.id)
                .fetch();

        return results.stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(voteAnswerItem.voteItem.id),
                        tuple -> Optional.ofNullable(tuple.get(voteAnswerItem.id.count())).orElse(0L)
                ));
    }
}
