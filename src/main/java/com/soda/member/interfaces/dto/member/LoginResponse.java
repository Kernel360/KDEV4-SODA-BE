package com.soda.member.interfaces.dto.member;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.soda.member.interfaces.dto.company.CompanyResponse;
import com.soda.member.domain.member.Member;
import com.soda.member.domain.member.MemberRole;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponse {
    private Long memberId;
    private String name;
    private String authId;
    private String email;
    private String position;
    private String phoneNumber;
    private MemberRole role;
    private boolean firstLogin;
    private CompanyResponse company;

    public static LoginResponse fromEntity(Member member) {
        boolean firstLogin = (member.getEmail() == null);
        MemberRole memberRole = member.getRole();
        return LoginResponse.builder()
                .memberId(member.getId())
                .name(member.getName())
                .authId(member.getAuthId())
                .email(member.getEmail())
                .position(member.getPosition())
                .phoneNumber(member.getPhoneNumber())
                .role(memberRole)
                .firstLogin(firstLogin)
                .company(CompanyResponse.fromEntity(member.getCompany()))
                .build();
    }
}
