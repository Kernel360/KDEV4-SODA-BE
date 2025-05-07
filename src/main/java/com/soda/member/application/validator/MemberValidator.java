package com.soda.member.application.validator;

import com.soda.global.response.GeneralException;
import com.soda.member.domain.member.Member;
import com.soda.member.domain.member.MemberErrorCode;
import com.soda.member.domain.member.MemberProvider;
import com.soda.member.domain.member.MemberRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberValidator {
    private final MemberProvider memberProvider;

    public void validateAdminAccess(Member member) {
        if (member == null || !member.isAdmin()) {
            log.warn("관리자 권한 검증 실패: memberId={}, role={}",
                    member != null ? member.getId() : "null",
                    member != null ? member.getRole() : "null");
            throw new GeneralException(MemberErrorCode.NOT_ADMIN);
        }
    }

    public void validateMemberStatus(Member member) {
        if (member == null) {
            throw new GeneralException(MemberErrorCode.NOT_FOUND_MEMBER);
        }

        if (member.getIsDeleted()) {
            log.warn("삭제된 회원 접근 시도: memberId={}", member.getId());
            throw new GeneralException(MemberErrorCode.DELETED_MEMBER);
        }
    }

    public void validateDuplicateEmail(String email) {
        if (memberProvider.existsByEmailAndIsDeletedFalse(email)) {
            log.warn("회원 가입/수정 실패: 이메일 중복 - {}", email);
            throw new GeneralException(MemberErrorCode.DUPLICATE_EMAIL);
        }
    }

    public void validateDuplicateAuthId(String authId) {
        if (memberProvider.existsByAuthId(authId)) {
            log.warn("회원 가입/수정 실패: 아이디 중복 - {}", authId);
            throw new GeneralException(MemberErrorCode.DUPLICATE_AUTH_ID);
        }
    }

    public void validateEmailExists(String email) {
        if (!memberProvider.existsByEmailAndIsDeletedFalse(email)) {
            log.warn("회원 조회/검증 실패: 존재하지 않는 이메일 - {}", email);
            throw new GeneralException(MemberErrorCode.NOT_FOUND_MEMBER);
        }
    }

    public void validateProfileUpdate(Member member, String name, String email, String phoneNumber) {
        validateMemberStatus(member);
        if (!member.getEmail().equals(email)) {
            validateDuplicateEmail(email);
        }
    }

    public void validatePasswordChange(Member member, String currentPassword, String newPassword) {
        validateMemberStatus(member);
        if (currentPassword.equals(newPassword)) {
            log.warn("비밀번호 변경 실패: 새 비밀번호가 현재 비밀번호와 동일함 - memberId={}", member.getId());
            throw new GeneralException(MemberErrorCode.INVALID_PASSWORD);
        }
    }

    public boolean isAdmin(MemberRole memberRole) {
        return memberRole == MemberRole.ADMIN;
    }
}