package com.soda.project.domain.dto;

import com.soda.project.domain.Project;
import com.soda.project.domain.enums.ProjectStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ProjectViewResponse {

    private Long id;
    private String title;
    private String description;
    private ProjectStatus status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    // 현재 로그인한 사용자의 역할 (개발사 또는 고객사 / 담당자 또는 일반 참여자)
    private String currentUserProjectRole;
    private String currentUserCompanyRole;

    // 고객사 정보
    private List<String> clientCompanyNames;
    private List<String> clientManagers;
    private List<String> clientMembers;

    // 개발사
    private List<String> devCompanyNames;
    private List<String> devManagers;
    private List<String> devMembers;

    public static ProjectViewResponse from(
            Project project,
            String currentUserProjectRole,
            String currentUserCompanyRole,
            List<String> clientCompanyNames,
            List<String> clientManagers,
            List<String> clientMembers,
            List<String> devCompanyNames,
            List<String> devManagers,
            List<String> devMembers
    ) {
        return ProjectViewResponse.builder()
                .id(project.getId())
                .title(project.getTitle())
                .description(project.getDescription())
                .status(project.getStatus())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .currentUserProjectRole((currentUserProjectRole))
                .currentUserCompanyRole(currentUserCompanyRole)
                .clientCompanyNames(clientCompanyNames)
                .clientManagers(clientManagers)
                .clientMembers(clientMembers)
                .devCompanyNames(devCompanyNames)
                .devManagers(devManagers)
                .devMembers(devMembers)
                .build();
    }

}
