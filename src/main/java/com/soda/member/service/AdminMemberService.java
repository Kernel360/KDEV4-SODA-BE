package com.soda.member.service;

import com.soda.global.response.GeneralException;
import com.soda.member.dto.UpdateUserStatusRequestDto;
import com.soda.member.entity.Member;
import com.soda.member.error.MemberErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminMemberService {

    private final MemberService memberService;

    /**
     * 사용자 활성/비활성 상태 변경
     *
     * @param userId          대상 사용자 ID
     * @param currentMemberId
     * @param requestDto      상태 변경 요청 DTO
     */
    @Transactional
    public void updateMemberStatus(Long userId, Long currentMemberId, UpdateUserStatusRequestDto requestDto) {
        Member member = memberService.findMemberById(userId);
        Member currentMember = memberService.findByIdAndIsDeletedFalse(currentMemberId);

         if (member.getAuthId().equals(currentMember.getAuthId()) && !requestDto.getActive()) {
             throw new GeneralException(MemberErrorCode.CANNOT_DEACTIVATE_SELF);
         }

        if(requestDto.getActive()){
            member.Active();
        }else {
            member.Deleted();
        }

        memberService.saveMember(member);
        log.info("관리자에 의해 사용자 상태 변경 완료: userId={}, active={}", userId, requestDto.getActive());
    }


}
