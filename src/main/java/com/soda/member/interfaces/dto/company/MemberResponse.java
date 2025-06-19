package com.soda.member.interfaces.dto.company;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.soda.member.domain.member.Member;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MemberResponse {
    private Long id;
    private String authId;
    private String name;
    private String email;
    private String position;
    private String phoneNumber;
    private String role;
    private String CompanyName;
    private Boolean isDeleted;

    public static MemberResponse fromEntity(Member member) {
        String companyNameResult = null;
        if (member.getCompany() != null) {
            companyNameResult = member.getCompany().getName();
        }
        return MemberResponse.builder()
                .id(member.getId())
                .authId(member.getAuthId())
                .name(member.getName())
                .email(member.getEmail())
                .position(member.getPosition())
                .phoneNumber(member.getPhoneNumber())
                .role(member.getRole().toString())
                .CompanyName(companyNameResult)
                .isDeleted(member.getIsDeleted())
                .build();
    }
}
