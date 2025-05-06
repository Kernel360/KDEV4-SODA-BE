package com.soda.project.interfaces.dto;

import com.soda.member.entity.Member;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class ProjectMemberAddResponse {

    private Long projectId;
    private String companyName;
    private List<String> managerNames;
    private List<String> memberNames;

    public static ProjectMemberAddResponse from (Long projectId, String companyName, List<Member> managers, List<Member> members) {
        return ProjectMemberAddResponse.builder()
                .projectId(projectId)
                .companyName(companyName)
                .managerNames(managers.stream()
                        .map(Member::getName)
                        .collect(Collectors.toList()))
                .memberNames(members.stream()
                        .map(Member::getName)
                        .collect(Collectors.toList()))
                .build();
    }
}
