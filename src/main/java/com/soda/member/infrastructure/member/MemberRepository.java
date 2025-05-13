package com.soda.member.infrastructure.member;

import com.soda.member.domain.company.Company;
import com.soda.member.domain.member.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {
    Optional<Member> findById(Long memberId);

    Optional<Member> findByAuthId(String authId);

    boolean existsByAuthId(String authId);

    Optional<Member> findByIdAndIsDeletedFalse(Long memberId);

    Optional<Member> findWithProjectsById(Long id);

    boolean existsByEmailAndIsDeletedFalse(String email);

    List<Member> findByIdInAndIsDeletedFalse(List<Long> memberIds);

    Optional<Member> findByNameAndEmailAndIsDeletedFalse(String name, String email);

    Optional<Member> findByAuthIdAndIsDeletedFalse(String authId);

    Optional<Member> findByEmailAndIsDeletedFalse(String email);

    Page<Member> findByKeywordIncludingDeletedOrderByCreatedAtDesc(String keyword, Pageable pageable);

    Page<Member> findAllByOrderByCreatedAtDesc(Pageable pageable);

}
