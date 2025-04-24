package com.soda.member.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberUpdateRequest {
    private String name;
    private String authId;
    private String password;
    private String email;
    private Long companyId;
    private String position;
    private String phoneNumber;
}

