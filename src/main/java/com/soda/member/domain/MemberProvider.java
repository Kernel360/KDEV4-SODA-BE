package com.soda.member.domain;

import com.soda.member.domain.company.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface MemberProvider {
    Optional<Member> findById(Long id);

    Optional<Member> findByIdAndIsDeletedFalse(Long id);

    Optional<Member> findByAuthIdAndIsDeletedFalse(String authId);

    Optional<Member> findByEmailAndIsDeletedFalse(String email);

    Optional<Member> findByNameAndEmailAndIsDeletedFalse(String name, String email);

    Optional<Member> findWithProjectsById(Long id);

    List<Member> findByIdInAndIsDeletedFalse(List<Long> ids);

    List<Member> findMembersByIdsAndCompany(List<Long> ids, Company company);

    Page<Member> findAll(Pageable pageable);

    Page<Member> findByKeywordIncludingDeleted(String keyword, Pageable pageable);

    boolean existsByAuthId(String authId);

    boolean existsByEmailAndIsDeletedFalse(String email);

    Member save(Member member);
}