package com.soda.member.dto;

import com.soda.member.enums.MemberRole;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberUpdateRequest {
    private String name;
    private String authId;
    private String email;
    private MemberRole role;
    private Long companyId;
    private String position;
    private String phoneNumber;
}

