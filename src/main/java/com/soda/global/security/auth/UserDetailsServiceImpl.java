package com.soda.global.security.auth;

import com.soda.member.domain.member.Member;
import com.soda.member.infrastructure.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Spring Security의 UserDetailsService 인터페이스 구현체입니다.
 * 사용자 인증 시 필요한 사용자 정보를 데이터베이스에서 조회하는 역할을 담당합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    private final MemberRepository memberRepository;

    /**
     * Spring Security가 인증 과정에서 호출하는 메소드입니다.
     * 주어진 인증 ID(authId)를 사용하여 데이터베이스에서 사용자 정보를 찾아 UserDetails 객체로 반환합니다.
     *
     * @param authId 로그인 시 사용되는 사용자의 고유 인증 ID (일반적으로 사용자명이나 이메일 등)
     * @return 인증 및 인가에 필요한 사용자 정보를 담은 UserDetails 객체
     * @throws UsernameNotFoundException 제공된 authId에 해당하는 사용자를 찾을 수 없거나,
     *                                   해당 사용자가 논리적으로 삭제(비활성) 상태일 경우 발생합니다.
     */
    @Override
    public UserDetails loadUserByUsername(String authId) throws UsernameNotFoundException {
        Member member = memberRepository.findByAuthId(authId)
                .orElseThrow(() -> {
                    log.warn("인증 ID '{}'에 해당하는 사용자를 찾을 수 없습니다.", authId);
                    return new UsernameNotFoundException("사용자를 찾을 수 없습니다 (인증 ID: " + authId + ")");
                });

        if (member.getIsDeleted()) {
            log.warn("삭제된 계정으로 로그인 시도됨: {}", authId);
            throw new UsernameNotFoundException("삭제된 계정입니다 (인증 ID: " + authId + ")");
        }

        log.debug("사용자 찾음 및 상세 정보 로드 완료 (인증 ID: {})", authId);
        return new UserDetailsImpl(member);
    }
}