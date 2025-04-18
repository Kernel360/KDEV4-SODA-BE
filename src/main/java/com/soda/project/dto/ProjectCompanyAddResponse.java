package com.soda.project.dto;

import com.soda.member.entity.Company;
import com.soda.member.entity.Member;
import com.soda.member.enums.CompanyProjectRole;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class ProjectCompanyAddResponse {

    private Long id;
    private String companyName;
    private CompanyProjectRole role;

    private List<String> managerNames;
    private List<String> memberNames;

    public static ProjectCompanyAddResponse from (
            Long projectId,
            Company addedCompany,
            CompanyProjectRole addedRole,
            List<Member> addedManagers,
            List<Member> addedMembers
    ) {
        return ProjectCompanyAddResponse.builder()
                .id(projectId)
                .companyName(addedCompany.getName())
                .role(addedRole)
                .managerNames(addedManagers.stream()
                        .map(Member::getName)
                        .collect(Collectors.toList()))
                .memberNames(addedMembers.stream()
                        .map(Member::getName)
                        .collect(Collectors.toList()))
                .build();
    }
}
