package com.soda.member.service;

import com.soda.global.response.ErrorCode;
import com.soda.global.response.GeneralException;
import com.soda.global.security.jwt.JwtTokenProvider;
import com.soda.member.dto.LoginRequestDto;
import com.soda.member.dto.LoginResponseDto;
import com.soda.member.dto.SignupRequestDto;
import com.soda.member.entity.Company;
import com.soda.member.entity.Member;
import com.soda.member.entity.RefreshToken;
import com.soda.member.repository.MemberRepository;
import com.soda.member.repository.RefreshTokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    private final JwtTokenProvider jwtTokenProvider;


    @Transactional
    public void signup(SignupRequestDto requestDto) {
        // 아이디 중복 확인
        if (memberRepository.existsByAuthId(requestDto.getAuthId())) {
            throw new GeneralException(ErrorCode.DUPLICATE_AUTH_ID);
        }

        // todo company생기면 수정해야함
        Company company = null;

        Member member = Member.builder()
                .authId(requestDto.getAuthId())
                .name(requestDto.getName())
                .password(passwordEncoder.encode(requestDto.getPassword()))
                .role(requestDto.getRole())
                .company(company)
                .position(requestDto.getPosition())
                .phoneNumber(requestDto.getPhoneNumber())
                .build();

        memberRepository.save(member);
    }


    @Transactional
    public LoginResponseDto login(LoginRequestDto requestDto, HttpServletRequest request) {
        Member member = memberRepository.findByAuthId(requestDto.getAuthId())
                .orElseThrow(() -> new GeneralException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(requestDto.getPassword(), member.getPassword())) {
            throw new GeneralException(ErrorCode.INVALID_CREDENTIALS);
        }

        String accessToken = jwtTokenProvider.createAccessToken(member.getAuthId());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getAuthId());

        // 기존 리프레시 토큰 삭제 후 새로운 토큰 저장
        refreshTokenRepository.deleteByAuthId(member.getAuthId());

        // 새로운 리프레시 토큰 저장
        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .authId(member.getAuthId())
                .token(refreshToken)
                .build();
        refreshTokenRepository.save(refreshTokenEntity);

        return LoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Transactional
    public LoginResponseDto refreshAccessToken(HttpServletRequest request) {
        String refreshToken = jwtTokenProvider.resolveToken(request);
        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND_REFRESH_TOKEN));

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new GeneralException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        String authId = storedToken.getAuthId();
        String newAccessToken = jwtTokenProvider.createAccessToken(authId);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(authId);

        // 토큰 초기화
        storedToken.setToken(newRefreshToken);

        return LoginResponseDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .build();
    }
}