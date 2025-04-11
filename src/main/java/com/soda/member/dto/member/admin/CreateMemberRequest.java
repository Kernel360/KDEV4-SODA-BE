package com.soda.member.dto.member.admin;

import com.soda.member.enums.MemberRole;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateMemberRequest {
    private String name;
    private String authId;
    private String password;
    private MemberRole role;
    private Long companyId;
}
