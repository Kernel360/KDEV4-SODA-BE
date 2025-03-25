package com.soda.global.security.auth;

import com.soda.global.response.ErrorCode;
import com.soda.global.response.GeneralException;
import com.soda.member.entity.Member;
import com.soda.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    private final MemberRepository memberRepository;


    @Override
    public UserDetails loadUserByUsername(String authId) throws UsernameNotFoundException {
        Member member = memberRepository.findByAuthId(authId)
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND));

        if (member.getIsDeleted()) {
            throw new GeneralException(ErrorCode.NOT_FOUND_MEMBER);
        }
        return new UserDetailsImpl(member);

    }
}
