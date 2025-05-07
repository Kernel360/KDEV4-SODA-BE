package com.soda.project.interfaces.dto;

import com.soda.member.domain.company.Company;
import com.soda.member.domain.Member;
import com.soda.project.domain.company.CompanyProjectRole;
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
