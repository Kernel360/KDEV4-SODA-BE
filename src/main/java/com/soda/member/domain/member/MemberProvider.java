package com.soda.member.domain.member;

import com.soda.member.entity.Member;

import java.util.List;

public interface MemberProvider {
    List<Member> findAllById(List<Long> Ids);
}
