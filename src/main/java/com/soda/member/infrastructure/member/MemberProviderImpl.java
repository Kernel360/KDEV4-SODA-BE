package com.soda.member.infrastructure.member;

import com.soda.member.domain.member.MemberProvider;
import com.soda.member.domain.Member;
import com.soda.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MemberProviderImpl implements MemberProvider {
    private final MemberRepository memberRepository;

    @Override
    public List<Member> findAllById(List<Long> Ids) {
        return memberRepository.findAllById(Ids);
    }
}
