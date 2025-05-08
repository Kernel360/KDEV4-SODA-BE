package com.soda.project.interfaces.dto;

import com.soda.member.domain.company.Company; // Company import
import com.soda.member.domain.member.Member;   // Member import
import com.soda.project.domain.company.CompanyProjectRole;
import com.soda.project.domain.member.MemberProject;
import com.soda.project.domain.member.MemberProjectRole; // 역할 확인용 (선택적)
import com.soda.project.domain.Project;
import com.soda.project.domain.ProjectStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
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

    // 고객사별 할당 정보
    private List<ClientAssignmentInfo> clientAssignments;

    public static ProjectCreateResponse from(Project project) {
        if (project == null) {
            return null;
        }

        // 프로젝트에 연결된 고객사(들) 정보 추출
        List<ClientAssignmentInfo> assignments = project.getCompanyProjects().stream()
                .filter(cp -> cp.getCompanyProjectRole() == CompanyProjectRole.CLIENT_COMPANY && !cp.getIsDeleted())
                .map(clientCompanyProject -> {
                    Company company = clientCompanyProject.getCompany();
                    if (company == null) return null;

                    // 해당 회사의 프로젝트 멤버들을 역할별로 필터링
                    List<Member> allCompanyMembersInProject = project.getMemberProjects().stream()
                            .filter(mp -> !mp.getIsDeleted() && mp.getMember().getCompany() != null
                                    && mp.getMember().getCompany().getId().equals(company.getId()))
                            .map(MemberProject::getMember)
                            .distinct() // 혹시 모를 중복 제거
                            .toList();

                    // 역할별 이름 추출
                    List<String> managerNames = allCompanyMembersInProject.stream()
                            .filter(m -> project.getMemberProjects().stream()
                                    .anyMatch(mp -> !mp.getIsDeleted() && mp.getMember().getId().equals(m.getId())
                                            && mp.getRole() == MemberProjectRole.CLI_MANAGER)
                            )
                            .map(Member::getName)
                            .collect(Collectors.toList());

                    List<String> memberNames = allCompanyMembersInProject.stream()
                            .filter(m -> project.getMemberProjects().stream()
                                    .anyMatch(mp -> !mp.getIsDeleted() && mp.getMember().getId().equals(m.getId()) && mp.getRole() == MemberProjectRole.CLI_PARTICIPANT)
                            )
                            .map(Member::getName)
                            .collect(Collectors.toList());

                    // ClientAssignmentInfo 생성
                    return ClientAssignmentInfo.builder()
                            .companyName(company.getName())
                            .managerNames(managerNames)
                            .memberNames(memberNames)
                            .build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 최종 응답 DTO 빌드
        return ProjectCreateResponse.builder()
                .id(project.getId())
                .title(project.getTitle())
                .description(project.getDescription())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .status(project.getStatus())
                .clientAssignments(assignments)
                .build();
    }

    // 멤버 이름 추출 헬퍼 메서드 (ProjectCreateResponse 내부에 둘 수 있음)
    private static List<String> extractMemberNames(List<Member> members) {
        if (members == null || members.isEmpty()) {
            return List.of();
        }
        return members.stream()
                .map(Member::getName)
                .collect(Collectors.toList());
    }

    @Getter
    @Builder
    public static class ClientAssignmentInfo {
        private String companyName;
        private List<String> managerNames;
        private List<String> memberNames;
    }
}