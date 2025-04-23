package com.soda.member.service;

import com.soda.global.response.GeneralException;
import com.soda.member.dto.FindAuthIdRequest;
import com.soda.member.dto.FindAuthIdResponse;
import com.soda.member.dto.InitialUserInfoRequestDto;
import com.soda.member.dto.member.ChangePasswordRequest;
import com.soda.member.dto.member.MemberStatusResponse;
import com.soda.member.dto.member.admin.MemberDetailDto;
import com.soda.member.entity.Member;
import com.soda.member.enums.MemberRole;
import com.soda.member.enums.MemberStatus;
import com.soda.member.error.MemberErrorCode;
import com.soda.member.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원 ID로 삭제되지 않은 회원 단 건 조회 (내부 또는 외부용 기본 ID 조회)
     *
     * @param memberId 회원 ID
     * @return 조회된 회원 엔티티
     * @throws GeneralException 회원을 찾을 수 없을 경우 발생
     */
    public Member findByIdAndIsDeletedFalse(Long memberId) {
        return memberRepository.findByIdAndIsDeletedFalse(memberId)
                .orElseThrow(() -> new GeneralException(MemberErrorCode.NOT_FOUND_MEMBER));
    }


    /**
     * 회원 ID로 회원 조회
     *
     * @param memberId 회원 ID
     * @return 조회된 회원 엔티티
     * @throws GeneralException 회원을 찾을 수 없을 경우 발생
     */
    public Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> {
                    log.warn("회원 조회 실패: ID로 회원을 찾을 수 없음 - {}", memberId);
                    return new GeneralException(MemberErrorCode.NOT_FOUND_MEMBER);
                });
    }

    /**
     * 인증 아이디(authId)로 삭제되지 않은 회원 조회
     *
     * @param authId 인증 아이디
     * @return 조회된 회원 엔티티
     * @throws GeneralException 회원을 찾을 수 없을 경우 발생
     */
    public Member findMemberByAuthId(String authId) {
        return memberRepository.findByAuthIdAndIsDeletedFalse(authId)
                .orElseThrow(() -> {
                    log.warn("회원 조회 실패: 유효하지 않은 authId - {}", authId);
                    return new GeneralException(MemberErrorCode.NOT_FOUND_MEMBER);
                });
    }

    public Member findMemberByEmail(String email) {
        return findMemberByEmailOrThrow(email);
    }

    /**
     * 여러 ID에 해당하는 활성 회원 목록 조회
     *
     * @param ids 회원 ID 목록
     * @return 조회된 회원 엔티티 목록
     */
    public List<Member> findByIds(List<Long> ids) {
        return memberRepository.findByIdInAndIsDeletedFalse(ids);
    }

    /**
     * 프로젝트 정보와 함께 회원 조회
     *
     * @param memberId 회원 ID
     * @return 프로젝트 정보를 포함한 회원 엔티티
     * @throws GeneralException 회원을 찾을 수 없을 경우 발생
     */
    public Member getMemberWithProjectOrThrow(Long memberId) {
        return memberRepository.findWithProjectsById(memberId)
                .orElseThrow(() -> new GeneralException(MemberErrorCode.NOT_FOUND_MEMBER));
    }


    /**
     * 이름과 이메일로 삭제되지 않은 사용자를 찾아 마스킹된 아이디를 반환 (아이디 찾기 기능).
     *
     * @param request 이름과 이메일이 담긴 요청 DTO
     * @return 마스킹된 아이디가 담긴 응답 DTO
     * @throws GeneralException 일치하는 삭제되지 않은 사용자를 찾지 못한 경우
     */
    public FindAuthIdResponse findMaskedAuthId(FindAuthIdRequest request) {
        log.info("아이디 찾기 시도: 이름={}, 이메일={}", request.getName(), request.getEmail());

        Member member = memberRepository.findByNameAndEmailAndIsDeletedFalse(request.getName(), request.getEmail())
                .orElseThrow(() -> {
                    log.warn("아이디 찾기 실패: 일치하는 사용자 없음 - 이름={}, 이메일={}", request.getName(), request.getEmail());
                    return new GeneralException(MemberErrorCode.NOT_FOUND_MEMBER);
                });

        String maskedId = maskAuthId(member.getAuthId());
        log.info("아이디 찾기 성공: 이름={}, 이메일={}. 마스킹된 아이디: {}", request.getName(), request.getEmail(), maskedId);

        return new FindAuthIdResponse(maskedId);
    }


    /**
     * 인증 아이디(authId) 중복 검사 (삭제되지 않은 회원 대상)
     *
     * @param authId 검사할 인증 아이디
     * @throws GeneralException 아이디가 이미 존재할 경우 발생
     */
    public void validateDuplicateAuthId(String authId) {
        if (memberRepository.existsByAuthId(authId)) {
            log.warn("회원 가입/수정 실패: 아이디 중복 - {}", authId);
            throw new GeneralException(MemberErrorCode.DUPLICATE_AUTH_ID);
        }
    }

    /**
     * 이메일 존재 여부 검증 (삭제되지 않은 회원 대상)
     *
     * @param email 검증할 이메일
     * @throws GeneralException 해당 이메일의 회원이 존재하지 않을 경우 발생
     */
    public void validateEmailExists(String email) {
        findMemberByEmailOrThrow(email);
    }

    /**
     * 회원 정보 저장 (생성 또는 수정)
     *
     * @param member 저장할 회원 엔티티
     */
    @Transactional
    public Member saveMember(Member member) {
        log.info("회원 정보 저장 완료: memberId={}", member.getId());
        return memberRepository.save(member);
    }

    /**
     * 해당 역할이 ADMIN인지 확인
     *
     * @param memberRole 검사할 회원 역할
     * @return ADMIN이면 true, 아니면 false
     */
    public boolean isAdmin(MemberRole memberRole) {
        return memberRole == MemberRole.ADMIN;
    }

    /**
     * (내부용) 이메일로 삭제되지 않은 회원을 찾거나 예외 발생
     *
     * @param email 찾을 이메일
     * @return 회원 엔티티
     * @throws GeneralException 회원을 찾을 수 없을 경우
     */
    private Member findMemberByEmailOrThrow(String email) {
        return memberRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> {
                    log.warn("회원 조회/검증 실패: 존재하지 않는 이메일 - {}", email);
                    return new GeneralException(MemberErrorCode.NOT_FOUND_MEMBER);
                });
    }

    /**
     * (내부용) 아이디를 마스킹 처리하는 헬퍼 메소드.
     *
     * @param authId 원본 아이디
     * @return 마스킹된 아이디
     */
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

    /**
     * 이메일 중복 검사 (삭제되지 않은 회원 대상)
     * 해당 이메일을 사용하는 활성 회원이 이미 존재하면 예외를 발생시킵니다.
     *
     * @param email 검사할 이메일
     * @throws GeneralException 해당 이메일이 이미 사용 중일 경우 (DUPLICATE_EMAIL)
     */
    public void validateDuplicateEmail(String email) {
        if (memberRepository.existsByEmailAndIsDeletedFalse(email)) {
            log.warn("회원 가입/수정 실패: 이메일 중복 - {}", email);
            throw new GeneralException(MemberErrorCode.DUPLICATE_EMAIL);
        }
    }

    /**
     * 삭제되지 않은 모든 회원 목록을 페이징 처리하여 조회합니다.
     * (주로 관리자 기능 등에서 전체 사용자 목록을 볼 때 사용됩니다.)
     *
     * @param pageable 페이징 및 정렬 정보를 담은 객체
     * @return 페이징된 회원 목록 (`Page` 객체)
     */
    public Page<Member> findAll(Pageable pageable) {
        return memberRepository.findAll(pageable);
    }

    /**
     * 삭제되지 않은 회원 중 특정 키워드와 일치하는 목록을 페이징 처리하여 조회합니다.
     * 검색 대상 필드는 Repository의 @Query 정의에 따릅니다.
     *
     * @param keyword 검색할 키워드
     * @param pageable 페이징 및 정렬 정보를 담은 객체
     * @return 검색 조건에 맞고 페이징된 회원 목록 (`Page` 객체)
     */
    public Page<Member> findByKeywordIncludingDeleted(String keyword, Pageable pageable) {
        return memberRepository.findByKeywordIncludingDeleted(keyword, pageable);
    }

    public void setupInitialProfile(Long memberId, InitialUserInfoRequestDto requestDto) {

        Member member = findByIdAndIsDeletedFalse(memberId);

        member.initialProfile(requestDto.getName(),
                requestDto.getEmail(),
                requestDto.getPhoneNumber(),
                requestDto.getAuthId(),
                passwordEncoder.encode(requestDto.getPassword()),
                requestDto.getPosition()
        );

        memberRepository.save(member);
    }

    public MemberDetailDto getMemberDetail(Long currentMemberId) {
        Member member = findByIdAndIsDeletedFalse(currentMemberId);
        return MemberDetailDto.fromEntity(member);
    }

    /**
     * 특정 멤버의 현재 상태를 조회합니다.
     *
     * @param memberId 조회할 멤버의 ID
     * @return 멤버 상태 정보 DTO
     * @throws EntityNotFoundException 해당 ID의 멤버가 없을 경우
     */
    @Transactional(readOnly = true)
    public MemberStatusResponse getMemberStatus(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("ID " + memberId + " 에 해당하는 멤버를 찾을 수 없습니다."));

        return MemberStatusResponse.fromEntity(member);
    }

    /**
     * 특정 멤버의 상태를 업데이트합니다.
     *
     * @param memberId    업데이트할 멤버의 ID
     * @param newStatus   새로운 멤버 상태
     * @return 업데이트된 멤버 상태 정보 DTO
     * @throws EntityNotFoundException 해당 ID의 멤버가 없을 경우
     */
    @Transactional
    public MemberStatusResponse updateMemberStatus(Long memberId, MemberStatus newStatus) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("ID " + memberId + " 에 해당하는 멤버를 찾을 수 없습니다."));

        member.updateMemberStatus(newStatus);

        memberRepository.save(member);
        return MemberStatusResponse.fromEntity(member);
    }

    @Transactional
    public void changeUserPassword(Long memberId, ChangePasswordRequest requestDto) {

        log.info("사용자 비밀번호 변경 시도: memberId={}", memberId);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> {
                    log.error("비밀번호 변경 실패: 사용자를 찾을 수 없음 - memberId={}", memberId);
                    return new GeneralException(MemberErrorCode.NOT_FOUND_MEMBER);
                });

        if (!passwordEncoder.matches(requestDto.getCurrentPassword(), member.getPassword())) {
            log.warn("비밀번호 변경 실패: 현재 비밀번호 불일치 - memberId={}", memberId);
            throw new GeneralException(MemberErrorCode.INVALID_CURRENT_PASSWORD);
        }

        if (passwordEncoder.matches(requestDto.getNewPassword(), member.getPassword())) {
            log.warn("비밀번호 변경 실패: 새 비밀번호가 현재 비밀번호와 동일 - memberId={}", memberId);
            throw new GeneralException(MemberErrorCode.NEW_PASSWORD_SAME_AS_OLD);
        }

        member.updatePassword(passwordEncoder.encode(requestDto.getNewPassword()));
         memberRepository.save(member);
        log.debug("사용자 비밀번호 업데이트 완료: memberId={}", memberId);

    }
}