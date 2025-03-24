package com.soda.member.repository;

import com.soda.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Member findByMemberId(Long memberId);

    Optional<Member> findByIdAndIsDeletedFalse(Long memberId);
}
