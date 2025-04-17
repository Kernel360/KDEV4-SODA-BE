package com.soda.project.dto;

import com.soda.member.entity.Company;
import com.soda.member.entity.Member;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class DevCompanyAssignmentResponse {

    private List<String> devCompanies;
    private List<String> devManagers;
    private List<String> devMembers;

    public static DevCompanyAssignmentResponse from (List<Company> devCompanies,
                                                     List<Member> devManagers, List<Member> devMembers) {
        return DevCompanyAssignmentResponse.builder()
                .devCompanies(devCompanies.stream()
                        .map(Company::getName)
                        .collect(Collectors.toList()))
                .devManagers(devManagers.stream()
                        .map(Member::getName)
                        .collect(Collectors.toList()))
                .devMembers(devMembers.stream()
                        .map(Member::getName)
                        .collect(Collectors.toList()))
                .build();
    }
}
