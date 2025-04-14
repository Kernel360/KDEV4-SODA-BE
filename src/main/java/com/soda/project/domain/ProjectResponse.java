package com.soda.project.domain;

import com.soda.member.enums.CompanyProjectRole;
import com.soda.member.enums.MemberProjectRole;
import com.soda.project.enums.ProjectStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectResponse {

    private Long id;
    private String title;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private ProjectStatus status;

    private String currentUserProjectRole;
    private String currentUserCompanyRole;

    // 고객사 정보
    private String clientCompanyName;      // 고객사 이름
    private List<String> clientCompanyManagers;   // 고객사 담당자 이름들
    private List<String> clientCompanyMembers;  // 고객사 일반 참여자 이름들

    // 개발사 정보
    private String devCompanyName;         // 개발사 이름
    private List<String> devCompanyManagers;      // 개발사 담당자 이름들
    private List<String> devCompanyMembers;   // 개발사 일반 참여자 이름들
}
