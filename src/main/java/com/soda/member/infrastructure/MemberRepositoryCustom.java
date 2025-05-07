package com.soda.member.infrastructure;

import com.soda.member.domain.member.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface MemberRepositoryCustom {
    Page<Member> findByKeywordIncludingDeleted(String keyword, Pageable pageable);

    Page<Member> findAllWithCompany(Pageable pageable);

    Page<Member> findByKeywordWithCompany(String keyword, Pageable pageable);

    Optional<Member> findByIdWithCompany(Long id);
}