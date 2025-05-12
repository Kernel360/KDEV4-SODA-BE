package com.soda.member.domain.member;

import com.soda.member.domain.company.Company;
import com.soda.member.interfaces.dto.member.admin.MemberDetailDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface MemberProvider {
    Member store(Member member);

    Optional<Member> findById(Long id);

    List<Member> findAllById(List<Long> ids);

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

    Page<Member> findAllWithCompany(Pageable pageable);

    Page<Member> findByKeywordWithCompany(String keyword, Pageable pageable);

    MemberDetailDto getMemberDetailWithCompany(Long userId);

    List<Member> findMembersByCompany(Company company);

    Optional<Member> findByNameAndEmail(String name, String email);
}