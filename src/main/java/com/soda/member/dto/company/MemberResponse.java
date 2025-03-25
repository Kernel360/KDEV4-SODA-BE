package com.soda.member.dto.company;

import com.soda.member.entity.Member;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberResponse {
    private Long id;
    private String authId;
    private String name;
    private String position;
    private String phoneNumber;
    private String role;

    public static MemberResponse fromEntity(Member member) {
        MemberResponse response = new MemberResponse();
        response.setId(member.getId());
        response.setAuthId(member.getAuthId());
        response.setName(member.getName());
        response.setPosition(member.getPosition());
        response.setPhoneNumber(member.getPhoneNumber());
        response.setRole(member.getRole().toString());
        return response;
    }
}
