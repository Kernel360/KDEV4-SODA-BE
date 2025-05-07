package com.soda.global.init;

import com.soda.member.domain.member.Member;
import com.soda.member.domain.member.MemberRole;
import com.soda.member.application.CompanyFacade;
import com.soda.member.infrastructure.MemberRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class InitialDataLoader {

    private final CompanyFacade companyFacade;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${init.admin.authid}")
    private String adminAuthId;

    @Value("${init.admin.password}")
    private String adminPassword;

    @PostConstruct
    public void init() {
        createAdminCompanyAndUser();
    }

    private void createAdminCompanyAndUser() {
        // 관리자 계정 존재 여부 확인
        Optional<Member> adminUserOptional = memberRepository.findByAuthId(adminAuthId);

        if (adminUserOptional.isEmpty()) {
            // 관리자 유저 생성
            Member adminUser = Member.builder()
                    .authId(adminAuthId)
                    .password(passwordEncoder.encode(adminPassword))
                    .name("Admin User")
                    .position("Admin Position")
                    .phoneNumber("010-1234-5678")
                    .email("admin@gmail.com")
                    .role(MemberRole.ADMIN)
                    .build();
            memberRepository.save(adminUser);
        }
    }
}
