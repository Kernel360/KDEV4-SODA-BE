package com.soda.member.domain.member;

import com.soda.member.domain.Member;

import java.util.List;

public interface MemberProvider {
    List<Member> findAllById(List<Long> Ids);
}
