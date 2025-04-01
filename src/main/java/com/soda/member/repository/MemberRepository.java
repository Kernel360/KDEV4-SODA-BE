package com.soda.member.repository;

import com.soda.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findById(Long memberId);

    Optional<Member> findByAuthId(String authId);

    boolean existsByAuthId(String authId);

    Optional<Member> findByIdAndIsDeletedFalse(Long memberId);

    Optional<Member> findByEmail(String email);

    // 차후 QueryDSL로 리팩토링하면 좋음
    @Query("SELECT m FROM Member m LEFT JOIN FETCH m.memberProjects WHERE m.id = :id")
    Optional<Member> findWithProjectsById(@Param("id") Long id);

    boolean existsByEmailAndIsDeletedFalse(String email);

    List<Member> findByIdInAndIsDeletedFalse(List<Long> memberIds);

    List<Member> findByIdIn(List<Long> ids);
}
