package com.soda.project.interfaces.dto;

import com.soda.member.enums.CompanyProjectRole;
import com.soda.member.enums.MemberProjectRole;
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
