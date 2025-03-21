package com.soda.member.repository;

import com.soda.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member,Long> {
    Optional<Member> findByAuthId(String authId);

    boolean existsByAuthId(String authId);
}
