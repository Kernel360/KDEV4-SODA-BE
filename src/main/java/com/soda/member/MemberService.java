package com.soda.member;

import com.soda.global.response.GeneralException;
import com.soda.member.dto.MemberUpdateRequest;
import com.soda.member.dto.SignupRequest;
import com.soda.member.enums.MemberRole;
import com.soda.member.error.MemberErrorCode;
import com.soda.member.repository.MemberRepository;
import com.soda.project.ProjectErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    public Member findByIdAndIsDeletedFalse(Long memberId) {
        return memberRepository.findByIdAndIsDeletedFalse(memberId)
                .orElseThrow(() -> new GeneralException(ProjectErrorCode.MEMBER_NOT_FOUND));
    }

    @Transactional
    public void saveMember(Member member) {
        memberRepository.save(member);
    }

    public Member findMemberByAuthId(String authId) {
        return memberRepository.findByAuthId(authId)
                .orElseThrow(() -> {
                    log.error("멤버 조회 실패: 잘못된 아이디 - {}", authId);
                    return new GeneralException(MemberErrorCode.NOT_FOUND_MEMBER);
                });
    }

    public Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("멤버 조회 실패: 이메일 없음 - {}", email);
                    return new GeneralException(MemberErrorCode.NOT_FOUND_MEMBER);
                });
    }

    public Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> {
                    log.error("멤버 조회 실패: 멤버를 찾을 수 없음 - {}", memberId);
                    return new GeneralException(MemberErrorCode.NOT_FOUND_MEMBER);
                });
    }

    @Transactional
    public void updateMemberPassword(Member member, String encodedPassword) {
        member.changePassword(encodedPassword);
        memberRepository.save(member);
        log.info("멤버 비밀번호 변경 성공: {}", member.getAuthId());
    }

    public void validateDuplicateAuthId(String authId) {
        if (memberRepository.existsByAuthId(authId)) {
            log.error("회원 가입 실패: 아이디 중복 - {}", authId);
            throw new GeneralException(MemberErrorCode.DUPLICATE_AUTH_ID);
        }
    }

    public Member createMember(SignupRequest requestDto, Company company, PasswordEncoder passwordEncoder) {
        return Member.builder()
                .authId(requestDto.getAuthId())
                .name(requestDto.getName())
                .password(passwordEncoder.encode(requestDto.getPassword()))
                .role(requestDto.getRole())
                .company(company)
                .position(requestDto.getPosition())
                .phoneNumber(requestDto.getPhoneNumber())
                .build();
    }

    public void validateEmailExists(String email) {
        if (!memberRepository.existsByEmailAndIsDeletedFalse(email)) {
            throw new GeneralException(MemberErrorCode.NOT_FOUND_MEMBER);
        }
    }

    @Transactional
    public void updateMember(Member member, MemberUpdateRequest request, Company company) {
        member.updateMember(request, company);
        memberRepository.save(member);
        log.info("멤버 정보 수정 성공: {}", member.getId());
    }


    public Member getMemberWithProjectOrThrow(Long memberId) {
        return memberRepository.findWithProjectsById(memberId)
                .orElseThrow(() -> new GeneralException(MemberErrorCode.NOT_FOUND_MEMBER));
    }

    public boolean isAdmin(MemberRole memberRole) {
        return memberRole == MemberRole.ADMIN;
    }


    public List<Member> findByIds(List<Long> ids) {
        return memberRepository.findByIdIn(ids);
    }
}
