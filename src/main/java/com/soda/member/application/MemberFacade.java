package com.soda.member.application;

import com.soda.member.domain.Member;
import com.soda.member.domain.MemberService;
import com.soda.member.domain.MemberStatus;
import com.soda.member.domain.company.Company;
import com.soda.member.interfaces.dto.*;
import com.soda.member.interfaces.dto.member.ChangePasswordRequest;
import com.soda.member.interfaces.dto.member.MemberStatusResponse;
import com.soda.member.interfaces.dto.member.admin.MemberDetailDto;
import com.soda.member.interfaces.dto.member.admin.MemberListDto;
import com.soda.member.interfaces.dto.member.admin.UpdateUserStatusRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberFacade {
    private final MemberService memberService;

    public FindAuthIdResponse findMaskedAuthId(FindAuthIdRequest request) {
        return memberService.findMaskedAuthId(request);
    }

    @Transactional
    public void setupInitialProfile(Long memberId, InitialUserInfoRequestDto requestDto) {
        memberService.setupInitialProfile(memberId, requestDto);
    }

    public MemberDetailDto getMemberDetail(Long userId) {
        return memberService.getMemberDetail(userId);
    }

    public MemberStatusResponse getMemberStatus(Long memberId) {
        return memberService.getMemberStatus(memberId);
    }

    @Transactional
    public MemberStatusResponse updateMemberStatus(Long memberId, MemberStatus newStatus) {
        return memberService.updateMemberStatus(memberId, newStatus);
    }

    @Transactional
    public void updateMyProfile(Long memberId, MemberUpdateRequest requestDto) {
        memberService.updateMyProfile(memberId, requestDto);
    }

    @Transactional
    public void changeUserPassword(Long memberId, ChangePasswordRequest requestDto) {
        memberService.changeUserPassword(memberId, requestDto);
    }

    @Transactional
    public void updateMemberStatus(Long userId, Long currentMemberId, UpdateUserStatusRequestDto requestDto) {
        memberService.updateMemberStatus(userId, currentMemberId, requestDto);
    }

    public Page<MemberListDto> getAllUsers(Pageable pageable, String searchKeyword) {
        return memberService.getAllUsers(pageable, searchKeyword);
    }

    @Transactional
    public MemberDetailDto updateMemberInfo(Long userId, AdminUpdateUserRequestDto requestDto) {
        return memberService.updateMemberInfo(userId, requestDto);
    }

    public Member findByIdAndIsDeletedFalse(Long memberId) {
        return memberService.findByIdAndIsDeletedFalse(memberId);
    }

    public Member findMemberById(Long memberId) {
        return memberService.findMemberById(memberId);
    }

    public Member findMemberByAuthId(String authId) {
        return memberService.findMemberByAuthId(authId);
    }

    public Member findMemberByEmail(String email) {
        return memberService.findMemberByEmail(email);
    }

    public List<Member> findByIds(List<Long> ids) {
        return memberService.findByIds(ids);
    }

    public Member getMemberWithProjectOrThrow(Long memberId) {
        return memberService.getMemberWithProjectOrThrow(memberId);
    }

    public void validateDuplicateEmail(String email) {
        memberService.validateDuplicateEmail(email);
    }

    public void validateDuplicateAuthId(String authId) {
        memberService.validateDuplicateAuthId(authId);
    }

    public void validateEmailExists(String email) {
        memberService.validateEmailExists(email);
    }

    public Member findWithProjectsById(Long memberId) {
        return memberService.findWithProjectsById(memberId);
    }

    public List<Member> findMembersByIdsAndCompany(List<Long> ids, Company company) {
        return memberService.findMembersByIdsAndCompany(ids, company);
    }
}