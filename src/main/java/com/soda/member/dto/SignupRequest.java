package com.soda.member.dto;

import com.soda.member.enums.MemberRole;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignupRequest {
    private String name;
    private String authId;
    private String password;
    private MemberRole role;
    private Long companId;
    private String position;
    private String phoneNumber;
}
