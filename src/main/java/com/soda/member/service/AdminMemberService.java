package com.soda.member.service;

import com.soda.global.response.GeneralException;
import com.soda.member.dto.AdminUpdateUserRequestDto;
import com.soda.member.dto.member.admin.MemberDetailDto;
import com.soda.member.dto.member.admin.MemberListDto;
import com.soda.member.dto.member.admin.UpdateUserStatusRequestDto;
import com.soda.member.entity.Company;
import com.soda.member.entity.Member;
import com.soda.member.error.MemberErrorCode;
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
    private final CompanyService companyService;

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

        if (requestDto.getActive()) {
            member.Active();
        } else {
            member.Deleted();
        }

        memberService.saveMember(member);
        log.info("관리자에 의해 사용자 상태 변경 완료: userId={}, active={}", userId, requestDto.getActive());
    }

    /**
     * 전체 사용자 목록 조회 (페이징 및 검색 기능 포함)
     *
     * @param pageable 페이징 정보
     * @param searchKeyword 검색어 (이름, 이메일, 아이디)
     * @return 페이징 처리된 사용자 목록 DTO
     */
    @Transactional(readOnly = true)
    public Page<MemberListDto> getAllUsers(Pageable pageable, String searchKeyword) {
        Page<Member> memberPage;

        if (StringUtils.hasText(searchKeyword)) {
            memberPage = memberService.findByKeywordIncludingDeleted(searchKeyword, pageable);
            log.info("관리자 사용자 목록 검색 조회: keyword={}, page={}, size={}", searchKeyword, pageable.getPageNumber(), pageable.getPageSize());
        } else {
            memberPage = memberService.findAll(pageable);
            log.info("관리자 전체 사용자 목록 조회: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        }

        return memberPage.map(MemberListDto::fromEntity);
    }

    /**
     * 특정 사용자의 상세 정보를 조회합니다.
     * (삭제된 사용자 포함하여 조회 가능)
     *
     * @param userId 조회할 사용자의 ID
     * @return 사용자 상세 정보 DTO
     * @throws GeneralException 사용자를 찾을 수 없을 경우
     */
    public MemberDetailDto getMemberDetail(Long userId) {
        Member member = memberService.findMemberById(userId);
        log.info("관리자 사용자 상세 조회: userId={}", userId);
        return MemberDetailDto.fromEntity(member);
    }

    /**
     * 관리자가 특정 사용자의 정보를 수정합니다.
     * (비밀번호, 활성/비활성 상태는 제외)
     *
     * @param userId     수정할 사용자의 ID
     * @param requestDto 수정할 사용자 정보 DTO
     * @return 수정된 사용자 상세 정보 DTO
     * @throws GeneralException 사용자를 찾을 수 없거나, 유효성 검사 실패 시
     */
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
            company = companyService.getCompany(requestDto.getCompanyId());
            log.info("사용자 소속 회사 변경/설정: companyId={}, companyName={}", company.getId(), company.getName());
        } else {
            log.info("사용자 소속 회사 없음(null)으로 설정");
        }

        member.updateAdminInfo(requestDto.getName(), requestDto.getEmail(), requestDto.getRole(), company, requestDto.getPosition(), requestDto.getPhoneNumber());

        Member updatedMember = memberService.saveMember(member);
        log.info("관리자에 의해 사용자 정보 수정 완료: userId={}", userId);

        return MemberDetailDto.fromEntity(updatedMember);
    }


}
