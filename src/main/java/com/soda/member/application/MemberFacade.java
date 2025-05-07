package com.soda.member.application;

import com.soda.global.response.GeneralException;
import com.soda.member.domain.member.Member;
import com.soda.member.domain.member.MemberService;
import com.soda.member.domain.member.MemberStatus;
import com.soda.member.domain.company.Company;
import com.soda.member.interfaces.dto.*;
import com.soda.member.interfaces.dto.member.ChangePasswordRequest;
import com.soda.member.interfaces.dto.member.MemberStatusResponse;
import com.soda.member.interfaces.dto.member.admin.MemberDetailDto;
import com.soda.member.interfaces.dto.member.admin.MemberListDto;
import com.soda.member.interfaces.dto.member.admin.UpdateUserStatusRequestDto;
import com.soda.member.application.validator.MemberValidator;
import com.soda.member.domain.member.MemberErrorCode;
import org.springframework.util.StringUtils;
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
    private final MemberValidator memberValidator;
    private final CompanyFacade companyFacade;

    public FindAuthIdResponse findMaskedAuthId(FindAuthIdRequest request) {
        Member member = memberService.findByNameAndEmail(request.getName(), request.getEmail());
        String maskedAuthId = memberService.maskAuthId(member.getAuthId());
        return new FindAuthIdResponse(maskedAuthId);
    }

    @Transactional
    public void setupInitialProfile(Long memberId, InitialUserInfoRequestDto requestDto) {
        Member member = memberService.findByIdAndIsDeletedFalse(memberId);
        memberService.setupInitialProfile(member, requestDto.getName(), requestDto.getEmail(),
                requestDto.getPhoneNumber(), requestDto.getAuthId(), requestDto.getPassword(),
                requestDto.getPosition());
    }

    public MemberDetailDto getMemberDetail(Long userId) {
        Member member = memberService.findMemberById(userId);
        return MemberDetailDto.fromEntity(member);
    }

    public MemberStatusResponse getMemberStatus(Long memberId) {
        Member member = memberService.findMemberById(memberId);
        return MemberStatusResponse.fromEntity(member);
    }

    @Transactional
    public MemberStatusResponse updateMemberStatus(Long memberId, MemberStatus newStatus) {
        Member member = memberService.findMemberById(memberId);
        member = memberService.updateMemberStatus(member, newStatus);
        return MemberStatusResponse.fromEntity(member);
    }

    @Transactional
    public void updateMyProfile(Long memberId, MemberUpdateRequest requestDto) {
        Member member = memberService.findByIdAndIsDeletedFalse(memberId);
        memberService.updateProfile(member, requestDto.getName(), requestDto.getEmail(),
                requestDto.getPhoneNumber(), requestDto.getPosition());
    }

    @Transactional
    public void changeUserPassword(Long memberId, ChangePasswordRequest requestDto) {
        Member member = memberService.findByIdAndIsDeletedFalse(memberId);
        memberService.changePassword(member, requestDto.getCurrentPassword(), requestDto.getNewPassword());
    }

    @Transactional
    public void updateMemberDeletionStatus(Long userId, Long currentMemberId, UpdateUserStatusRequestDto requestDto) {
        Member member = memberService.findMemberById(userId);
        Member currentMember = memberService.findByIdAndIsDeletedFalse(currentMemberId);
        memberValidator.validateAdminAccess(currentMember);

        if (member.getAuthId().equals(currentMember.getAuthId()) && !requestDto.getActive()) {
            throw new GeneralException(MemberErrorCode.CANNOT_DEACTIVATE_SELF);
        }

        memberService.updateMemberDeletionStatus(member, !requestDto.getActive());
        log.info("관리자에 의해 사용자 상태 변경 완료: userId={}, active={}", userId, requestDto.getActive());
    }

    public Page<MemberListDto> getAllUsers(Pageable pageable, String searchKeyword) {
        Page<Member> memberPage;
        if (StringUtils.hasText(searchKeyword)) {
            memberPage = memberService.findByKeywordIncludingDeleted(searchKeyword, pageable);
            log.info("관리자 사용자 목록 검색 조회: keyword={}, page={}, size={}", searchKeyword, pageable.getPageNumber(),
                    pageable.getPageSize());
        } else {
            memberPage = memberService.findAll(pageable);
            log.info("관리자 전체 사용자 목록 조회: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        }
        return memberPage.map(MemberListDto::fromEntity);
    }

    @Transactional
    public MemberDetailDto updateMemberInfo(Long userId, AdminUpdateUserRequestDto requestDto) {
        Member member = memberService.findMemberById(userId);
        Company company = requestDto.getCompanyId() != null ? companyFacade.getCompany(requestDto.getCompanyId())
                : member.getCompany();

        member = memberService.updateAdminInfo(member, requestDto.getName(), requestDto.getEmail(),
                requestDto.getRole(), company, requestDto.getPosition(), requestDto.getPhoneNumber());

        log.info("관리자에 의해 사용자 정보 수정 완료: userId={}", userId);
        return MemberDetailDto.fromEntity(member);
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
        memberValidator.validateDuplicateEmail(email);
    }

    public void validateDuplicateAuthId(String authId) {
        memberValidator.validateDuplicateAuthId(authId);
    }

    public void validateEmailExists(String email) {
        memberValidator.validateEmailExists(email);
    }

    public Member findWithProjectsById(Long memberId) {
        return memberService.findWithProjectsById(memberId);
    }

    public List<Member> findMembersByIdsAndCompany(List<Long> ids, Company company) {
        return memberService.findMembersByIdsAndCompany(ids, company);
    }
}