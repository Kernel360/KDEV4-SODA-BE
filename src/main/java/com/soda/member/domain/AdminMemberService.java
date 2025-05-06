package com.soda.member.domain;

import com.soda.global.response.GeneralException;
import com.soda.member.domain.company.Company;
import com.soda.member.interfaces.dto.AdminUpdateUserRequestDto;
import com.soda.member.interfaces.dto.member.admin.MemberDetailDto;
import com.soda.member.interfaces.dto.member.admin.MemberListDto;
import com.soda.member.interfaces.dto.member.admin.UpdateUserStatusRequestDto;
import com.soda.member.application.CompanyFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminMemberService {

    private final MemberService memberService;
    private final CompanyFacade companyFacade;

    @Transactional
    public void updateMemberStatus(Long userId, Long currentMemberId, UpdateUserStatusRequestDto requestDto) {
        Member member = memberService.findMemberById(userId);
        Member currentMember = memberService.findByIdAndIsDeletedFalse(currentMemberId);

        if (member.getAuthId().equals(currentMember.getAuthId()) && !requestDto.getActive()) {
            throw new GeneralException(MemberErrorCode.CANNOT_DEACTIVATE_SELF);
        }

        if (requestDto.getActive()) {
            member.Active();
        } else {
            member.Deleted();
        }

        memberService.saveMember(member);
        log.info("관리자에 의해 사용자 상태 변경 완료: userId={}, active={}", userId, requestDto.getActive());
    }

    @Transactional(readOnly = true)
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

    public MemberDetailDto getMemberDetail(Long userId) {
        Member member = memberService.findMemberById(userId);
        log.info("관리자 사용자 상세 조회: userId={}", userId);
        return MemberDetailDto.fromEntity(member);
    }

    @Transactional
    public MemberDetailDto updateMemberInfo(Long userId, AdminUpdateUserRequestDto requestDto) {
        Member member = memberService.findMemberById(userId);
        log.info("관리자 사용자 정보 수정 시도: targetUserId={}", userId);

        if (!member.getEmail().equalsIgnoreCase(requestDto.getEmail())) {
            log.info("사용자 이메일 변경 감지: old={}, new={}", member.getEmail(), requestDto.getEmail());
            memberService.validateDuplicateEmail(requestDto.getEmail());
        }

        Company company = null;
        if (requestDto.getCompanyId() != null) {
            company = companyFacade.getCompany(requestDto.getCompanyId());
            log.info("사용자 소속 회사 변경/설정: companyId={}, companyName={}", company.getId(), company.getName());
        } else {
            log.info("사용자 소속 회사 없음(null)으로 설정");
        }

        member.updateAdminInfo(requestDto.getName(), requestDto.getEmail(), requestDto.getRole(), company,
                requestDto.getPosition(), requestDto.getPhoneNumber());

        Member updatedMember = memberService.saveMember(member);
        log.info("관리자에 의해 사용자 정보 수정 완료: userId={}", userId);

        return MemberDetailDto.fromEntity(updatedMember);
    }


}
