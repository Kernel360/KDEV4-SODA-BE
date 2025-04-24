package com.soda.member.dto.member;

import com.soda.member.entity.Member;
import com.soda.member.enums.MemberStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberStatusResponse {
    private Long memberId;
    private String memberName;
    private MemberStatus currentStatus;
    private String statusDescription;

    public static MemberStatusResponse fromEntity(Member member) {
        return new MemberStatusResponse(
                member.getId(),
                member.getName(),
                member.getMemberStatus(),
                member.getMemberStatus().getDescription()
        );
    }
}
