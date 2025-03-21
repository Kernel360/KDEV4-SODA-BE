package com.soda.global.config;

import com.soda.member.entity.Company;
import com.soda.member.entity.Member;
import com.soda.member.enums.MemberRole;
import com.soda.member.repository.CompanyRepository;
import com.soda.member.repository.MemberRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InitialDataLoader {

    private final CompanyRepository companyRepository;
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
        // 관리자 회사 생성
        Company adminCompany = Company.builder()
                .name("Admin Company")
                .phoneNumber("010-0000-0000")
                .companyNumber("000-00-00000")
                .address("Admin Address")
                .detailAddress("Admin Detail Address")
                .build();
        companyRepository.save(adminCompany);

        // 관리자 유저 생성
        Member adminUser = Member.builder()
                .authId(adminAuthId)
                .password(passwordEncoder.encode(adminPassword))
                .name("Admin User")
                .position("Admin Position")
                .phoneNumber("010-1234-5678")
                .role(MemberRole.ADMIN)
                .company(adminCompany)
                .build();
        memberRepository.save(adminUser);
    }
}
