package com.soda.member.repository;

import com.soda.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    Optional<Member> findByNameAndEmailAndIsDeletedFalse(String name, String email);

    Optional<Member> findByAuthIdAndIsDeletedFalse(String authId);

    Optional<Member> findByEmailAndIsDeletedFalse(String email);

    @Query("SELECT m FROM Member m LEFT JOIN m.company c WHERE " +
            "LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +           // 이름 검색
            "OR LOWER(m.email) LIKE LOWER(CONCAT('%', :keyword, '%')) " +         // 이메일 검색
            "OR LOWER(m.authId) LIKE LOWER(CONCAT('%', :keyword, '%')) " +        // 아이디 검색
            "OR (c.name IS NOT NULL AND LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Member> findByKeywordIncludingDeleted(@Param("keyword") String keyword, Pageable pageable);

}
