package com.soda.project.interfaces.dto;

import com.soda.member.domain.company.Company;
import com.soda.member.domain.member.Member;
import com.soda.project.domain.member.MemberProjectRole;
import com.soda.project.domain.member.MemberProject;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProjectMemberResponse {

    private Long companyId;
    private String companyName;
    private Long memberId;
    private String memberName;
    private MemberProjectRole role;
    private String memberStatus;

    public static ProjectMemberResponse from(MemberProject memberProject) {
        Member member = memberProject.getMember();
        Company company = (member != null) ? member.getCompany() : null;

        return ProjectMemberResponse.builder()
                .companyId(company != null ? company.getId() : null)
                .companyName(company != null ? company.getName() : "소속 회사 정보 없음")
                .memberId(member != null ? member.getId() : null)
                .memberName(member != null ? member.getName() : "알 수 없는 멤버")
                .role(memberProject.getRole())
                .memberStatus(member != null ? member.getMemberStatus().getDescription():null)
                .build();
    }

}
