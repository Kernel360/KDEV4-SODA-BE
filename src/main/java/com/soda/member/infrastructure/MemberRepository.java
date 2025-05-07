package com.soda.member.infrastructure;

import com.soda.member.domain.Member;
import com.soda.member.domain.company.Company;
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

    Optional<Member> findByEmail(String email);

    Optional<Member> findWithProjectsById(Long id);

    boolean existsByEmailAndIsDeletedFalse(String email);

    List<Member> findByIdInAndIsDeletedFalse(List<Long> memberIds);

    List<Member> findByIdIn(List<Long> ids);

    Optional<Member> findByNameAndEmailAndIsDeletedFalse(String name, String email);

    Optional<Member> findByAuthIdAndIsDeletedFalse(String authId);

    Optional<Member> findByEmailAndIsDeletedFalse(String email);

    List<Member> findByCompanyAndIsDeletedFalse(Company company);

    Page<Member> findByKeywordIncludingDeleted(String keyword, Pageable pageable);

    Page<Member> findAllWithCompany(Pageable pageable);

    Page<Member> findByKeywordWithCompany(String keyword, Pageable pageable);

    Optional<Member> findByIdWithCompany(Long id);
}
