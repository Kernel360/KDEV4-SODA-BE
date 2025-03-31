package com.soda.member.service;

import com.soda.global.response.GeneralException;
import com.soda.member.entity.Member;
import com.soda.member.repository.MemberRepository;
import com.soda.project.error.ProjectErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;

    public Member findByIdAndIsDeletedFalse(Long memberId) {
        return memberRepository.findByIdAndIsDeletedFalse(memberId)
                .orElseThrow(() -> new GeneralException(ProjectErrorCode.MEMBER_NOT_FOUND));
    }
}
