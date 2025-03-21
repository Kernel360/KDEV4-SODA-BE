package com.soda.member.dto;

import com.soda.member.enums.MemberRole;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequestDto {
    private String name;
    private String authId;
    private String password;
    private MemberRole role;
    private String companyName;
    private String position;
    private String phoneNumber;
}
