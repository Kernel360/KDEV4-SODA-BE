package com.soda.project.interfaces.dto;

import com.soda.project.domain.company.enums.CompanyProjectRole;
import com.soda.project.domain.member.enums.MemberProjectRole;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProjectMemberSearchCondition {

    private CompanyProjectRole companyRole;
    private Long companyId;
    private MemberProjectRole memberRole;
    private Long memberId;

}
