package com.soda.member.domain.member;

import com.soda.global.response.GeneralException;
import com.soda.member.domain.AuthErrorCode;
import com.soda.member.domain.company.Company;
import com.soda.member.interfaces.dto.FindAuthIdRequest;
import com.soda.member.interfaces.dto.FindAuthIdResponse;
import com.soda.member.interfaces.dto.member.admin.MemberListDto;
import com.soda.project.domain.ProjectErrorCode;
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
public class MemberService {

    private final MemberProvider memberProvider;
    private final PasswordEncoder passwordEncoder;

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

    public Member updateMemberStatus(Member member, MemberStatus newStatus) {
        member.updateMemberStatus(newStatus);
        return memberProvider.store(member);
    }

    public void updateMemberDeletionStatus(Member member, boolean isDeleted) {
        if (isDeleted) {
            member.delete();
        } else {
            member.restore();
        }
        memberProvider.store(member);
    }

    @Transactional
    public Member updateProfile(Member member, String name, String email, String phoneNumber, String position) {
        member.myProfileUpdate(name, email, phoneNumber, position);
        return memberProvider.store(member);
    }

    @Transactional
    public Member changePassword(Member member, String currentPassword, String newPassword) {
        if (!passwordEncoder.matches(currentPassword, member.getPassword())) {
            throw new GeneralException(MemberErrorCode.INVALID_PASSWORD);
        }

        member.updatePassword(passwordEncoder.encode(newPassword));
        return memberProvider.store(member);
    }

    public Member findWithProjectsById(Long memberId) {
        return memberProvider.findWithProjectsById(memberId)
                .orElseThrow(() -> new GeneralException(ProjectErrorCode.MEMBER_NOT_FOUND));
    }

    public List<Member> findMembersByIdsAndCompany(List<Long> ids, Company company) {
        return memberProvider.findMembersByIdsAndCompany(ids, company);
    }

    public Page<Member> findAllByOrderByCreatedAtDesc(Pageable pageable) {
        return memberProvider.findAllByOrderByCreatedAtDesc(pageable);
    }

    public Page<Member> findByKeywordIncludingDeletedOrderByCreatedAtDesc(String keyword, Pageable pageable) {
        return memberProvider.findByKeywordIncludingDeletedOrderByCreatedAtDesc(keyword, pageable);
    }

    @Transactional
    public void setupInitialProfile(Member member, String name, String email, String phoneNumber,
            String authId, String password, String position) {
        member.initialProfile(name, email, phoneNumber, authId, passwordEncoder.encode(password), position);
        memberProvider.store(member);
    }

    @Transactional
    public Member updateAdminInfo(Member member, String name, String email, MemberRole role,
            Company company, String position, String phoneNumber) {
        member.updateAdminInfo(name, email, role, company, position, phoneNumber);
        return memberProvider.store(member);
    }

    public String maskAuthId(String authId) {
        if (!StringUtils.hasText(authId)) {
            return "";
        }
        int length = authId.length();
        if (length <= 4) {
            return "*".repeat(length);
        }
        return authId.substring(0, 2) + "*".repeat(length - 4) + authId.substring(length - 2);
    }

    public Member findByNameAndEmail(String name, String email) {
        return memberProvider.findByNameAndEmail(name, email)
                .orElseThrow(() -> new GeneralException(MemberErrorCode.NOT_FOUND_MEMBER));
    }

    public boolean validateEmailExists(String email) {
        return memberProvider.existsByEmailAndIsDeletedFalse(email);
    }

    public void validateDuplicateAuthId(String authId) {
        boolean isExists = memberProvider.existsByAuthId(authId);
        if (isExists) {
            throw new GeneralException(MemberErrorCode.DUPLICATE_AUTH_ID);
        }
    }
}