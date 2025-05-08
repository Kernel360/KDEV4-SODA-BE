package com.soda.member.interfaces.dto.company;

import com.soda.member.domain.member.Member;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberResponse {
    private Long id;
    private String authId;
    private String name;
    private String position;
    private String phoneNumber;
    private String role;

    public static MemberResponse fromEntity(Member member) {
        return MemberResponse.builder()
                .id(member.getId())
                .authId(member.getAuthId())
                .name(member.getName())
                .position(member.getPosition())
                .phoneNumber(member.getPhoneNumber())
                .role(member.getRole().toString())
                .build();
    }
}
