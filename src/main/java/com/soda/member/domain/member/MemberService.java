package com.soda.member.domain.member;

import com.soda.global.response.GeneralException;
import com.soda.member.application.CompanyFacade;
import com.soda.member.application.validator.MemberValidator;
import com.soda.member.domain.company.Company;
import com.soda.member.interfaces.dto.FindAuthIdRequest;
import com.soda.member.interfaces.dto.FindAuthIdResponse;
import com.soda.member.interfaces.dto.InitialUserInfoRequestDto;
import com.soda.member.interfaces.dto.MemberUpdateRequest;
import com.soda.member.interfaces.dto.AdminUpdateUserRequestDto;
import com.soda.member.interfaces.dto.member.ChangePasswordRequest;
import com.soda.member.interfaces.dto.member.MemberStatusResponse;
import com.soda.member.interfaces.dto.member.admin.MemberDetailDto;
import com.soda.member.interfaces.dto.member.admin.MemberListDto;
import com.soda.member.interfaces.dto.member.admin.UpdateUserStatusRequestDto;
import com.soda.project.domain.ProjectErrorCode;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberProvider memberProvider;
    private final PasswordEncoder passwordEncoder;
    private final MemberValidator memberValidator;
    private final CompanyFacade companyFacade;

    public Member findByIdAndIsDeletedFalse(Long memberId) {
        return memberProvider.findByIdAndIsDeletedFalse(memberId)
                .orElseThrow(() -> new GeneralException(MemberErrorCode.NOT_FOUND_MEMBER));
    }

    public Member findMemberById(Long memberId) {
        return memberProvider.findById(memberId)
                .orElseThrow(() -> {
                    log.warn("회원 조회 실패: ID로 회원을 찾을 수 없음 - {}", memberId);
                    return new GeneralException(MemberErrorCode.NOT_FOUND_MEMBER);
                });
    }

    public Member findMemberByAuthId(String authId) {
        return memberProvider.findByAuthIdAndIsDeletedFalse(authId)
                .orElseThrow(() -> {
                    log.warn("회원 조회 실패: 유효하지 않은 authId - {}", authId);
                    return new GeneralException(MemberErrorCode.NOT_FOUND_MEMBER);
                });
    }

    public Member findMemberByEmail(String email) {
        return findMemberByEmailOrThrow(email);
    }

    public List<Member> findByIds(List<Long> ids) {
        return memberProvider.findByIdInAndIsDeletedFalse(ids);
    }

    public Member getMemberWithProjectOrThrow(Long memberId) {
        return memberProvider.findWithProjectsById(memberId)
                .orElseThrow(() -> new GeneralException(MemberErrorCode.NOT_FOUND_MEMBER));
    }

    public FindAuthIdResponse findMaskedAuthId(FindAuthIdRequest request) {
        Member member = memberProvider.findByNameAndEmailAndIsDeletedFalse(request.getName(), request.getEmail())
                .orElseThrow(() -> new GeneralException(MemberErrorCode.NOT_FOUND_MEMBER));

        String maskedAuthId = maskAuthId(member.getAuthId());
        return new FindAuthIdResponse(maskedAuthId);
    }

    public void validateDuplicateAuthId(String authId) {
        if (memberProvider.existsByAuthId(authId)) {
            log.warn("회원 가입/수정 실패: 아이디 중복 - {}", authId);
            throw new GeneralException(MemberErrorCode.DUPLICATE_AUTH_ID);
        }
    }

    public void validateEmailExists(String email) {
        findMemberByEmailOrThrow(email);
    }

    public void validateDuplicateEmail(String email) {
        if (memberProvider.existsByEmailAndIsDeletedFalse(email)) {
            log.warn("회원 가입/수정 실패: 이메일 중복 - {}", email);
            throw new GeneralException(MemberErrorCode.DUPLICATE_EMAIL);
        }
    }

    @Transactional
    public Member saveMember(Member member) {
        log.info("회원 정보 저장 완료: memberId={}", member.getId());
        return memberProvider.store(member);
    }

    private Member findMemberByEmailOrThrow(String email) {
        return memberProvider.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> {
                    log.warn("회원 조회/검증 실패: 존재하지 않는 이메일 - {}", email);
                    return new GeneralException(MemberErrorCode.NOT_FOUND_MEMBER);
                });
    }

    private String maskAuthId(String authId) {
        if (authId == null || authId.isEmpty()) {
            return "***";
        }
        int length = authId.length();
        if (length <= 1) {
            return "*";
        } else if (length <= 3) {
            return authId.charAt(0) + "*".repeat(length - 1);
        } else {
            return authId.substring(0, 2) + "***" + authId.charAt(length - 1);
        }
    }

    public Page<Member> findAll(Pageable pageable) {
        return memberProvider.findAllWithCompany(pageable);
    }

    public Page<Member> findByKeywordIncludingDeleted(String keyword, Pageable pageable) {
        return memberProvider.findByKeywordWithCompany(keyword, pageable);
    }

    public void setupInitialProfile(Long memberId, InitialUserInfoRequestDto requestDto) {
        Member member = findByIdAndIsDeletedFalse(memberId);

        member.initialProfile(requestDto.getName(),
                requestDto.getEmail(),
                requestDto.getPhoneNumber(),
                requestDto.getAuthId(),
                passwordEncoder.encode(requestDto.getPassword()),
                requestDto.getPosition());

        memberProvider.store(member);
    }

    public MemberDetailDto getMemberDetail(Long userId) {
        return memberProvider.getMemberDetailWithCompany(userId);
    }

    @Transactional(readOnly = true)
    public MemberStatusResponse getMemberStatus(Long memberId) {
        Member member = memberProvider.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("ID " + memberId + " 에 해당하는 멤버를 찾을 수 없습니다."));

        return MemberStatusResponse.fromEntity(member);
    }

    public MemberStatusResponse updateMemberStatus(Long memberId, MemberStatus newStatus) {
        Member member = memberProvider.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("ID " + memberId + " 에 해당하는 멤버를 찾을 수 없습니다."));

        member.updateMemberStatus(newStatus);

        memberProvider.store(member);
        return MemberStatusResponse.fromEntity(member);
    }

    public void updateMyProfile(Long memberId, MemberUpdateRequest requestDto) {
        Member member = findByIdAndIsDeletedFalse(memberId);

        member.myProfileUpdate(requestDto.getName(),
                requestDto.getEmail(),
                requestDto.getPhoneNumber(),
                requestDto.getPosition());

        memberProvider.store(member);
    }

    public void changeUserPassword(Long memberId, ChangePasswordRequest requestDto) {
        Member member = findByIdAndIsDeletedFalse(memberId);

        if (!passwordEncoder.matches(requestDto.getCurrentPassword(), member.getPassword())) {
            throw new GeneralException(MemberErrorCode.INVALID_PASSWORD);
        }

        member.updatePassword(passwordEncoder.encode(requestDto.getNewPassword()));
        memberProvider.store(member);
    }

    public Member findWithProjectsById(Long memberId) {
        return memberProvider.findWithProjectsById(memberId)
                .orElseThrow(() -> new GeneralException(ProjectErrorCode.MEMBER_NOT_FOUND));
    }

    public List<Member> findMembersByIdsAndCompany(List<Long> ids, Company company) {
        return memberProvider.findMembersByIdsAndCompany(ids, company);
    }

    public void updateMemberStatus(Long userId, Long currentMemberId, UpdateUserStatusRequestDto requestDto) {
        Member member = findMemberById(userId);
        Member currentMember = findByIdAndIsDeletedFalse(currentMemberId);
        memberValidator.validateAdminAccess(currentMember);

        if (member.getAuthId().equals(currentMember.getAuthId()) && !requestDto.getActive()) {
            throw new GeneralException(MemberErrorCode.CANNOT_DEACTIVATE_SELF);
        }

        if (requestDto.getActive()) {
            member.restore();
        } else {
            member.delete();
        }

        saveMember(member);
        log.info("관리자에 의해 사용자 상태 변경 완료: userId={}, active={}", userId, requestDto.getActive());
    }

    public Page<MemberListDto> getAllUsers(Pageable pageable, String searchKeyword) {
        Page<Member> memberPage;

        if (StringUtils.hasText(searchKeyword)) {
            memberPage = findByKeywordIncludingDeleted(searchKeyword, pageable);
            log.info("관리자 사용자 목록 검색 조회: keyword={}, page={}, size={}", searchKeyword, pageable.getPageNumber(),
                    pageable.getPageSize());
        } else {
            memberPage = findAll(pageable);
            log.info("관리자 전체 사용자 목록 조회: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        }

        return memberPage.map(MemberListDto::fromEntity);
    }

    public MemberDetailDto updateMemberInfo(Long userId, AdminUpdateUserRequestDto requestDto) {
        Member member = findMemberById(userId);
        Company company = requestDto.getCompanyId() != null ? companyFacade.getCompany(requestDto.getCompanyId())
                : member.getCompany();

        member.updateAdminInfo(
                requestDto.getName(),
                requestDto.getEmail(),
                requestDto.getRole(),
                company,
                requestDto.getPosition(),
                requestDto.getPhoneNumber());

        saveMember(member);
        log.info("관리자에 의해 사용자 정보 수정 완료: userId={}", userId);
        return MemberDetailDto.fromEntity(member);
    }

    public boolean isAdmin(MemberRole memberRole) {
        return memberRole == MemberRole.ADMIN;
    }
}