package com.soda.member.dto;

import com.soda.member.enums.MemberRole;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateMemberRequest {
    private String name;
    private String authId;
    private String password;
    private String email;
    private MemberRole role;
    private Long companyId;
    private String position;
    private String phoneNumber;
}
