package com.soda.project.dto;

import com.soda.member.entity.Company;
import com.soda.member.entity.Member;
import com.soda.project.entity.Project;
import com.soda.project.enums.ProjectStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class ProjectCreateResponse {

    // 프로젝트 기본 정보
    private Long id;
    private String title;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private ProjectStatus status;

    // 고객사, 고객사 담당자, 고객사 일반 참여자
    private List<String> clientCompanies;
    private List<String> clientManagers;
    private List<String> clientMembers;

    public static ProjectCreateResponse from(Project project, List<Company> clientCompanies,
                                             List<Member> clientManagers, List<Member> clientMembers) {
        return ProjectCreateResponse.builder()
                .id(project.getId())
                .title(project.getTitle())
                .description(project.getDescription())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .status(project.getStatus())
                .clientCompanies(clientCompanies.stream()
                        .map(Company::getName)
                        .collect(Collectors.toList()))
                .clientManagers(clientManagers.stream()
                        .map(Member::getName)
                        .collect(Collectors.toList()))
                .clientMembers(clientMembers.stream()
                        .map(Member::getName)
                        .collect(Collectors.toList()))
                .build();
    }

}
