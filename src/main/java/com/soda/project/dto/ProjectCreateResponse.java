package com.soda.project.dto;

import com.soda.member.entity.Company; // Company import
import com.soda.member.entity.Member;   // Member import
import com.soda.member.enums.MemberProjectRole; // 역할 확인용 (선택적)
import com.soda.project.entity.Project;
import com.soda.project.enums.ProjectStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map; // Map 사용 예시
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

    public static ProjectCreateResponse from(Project project,
                                             Map<Company, Map<MemberProjectRole, List<Member>>> clientAssignmentData) {

        // Map 데이터를 List<ClientAssignmentInfo>로 변환
        List<ClientAssignmentInfo> assignments = clientAssignmentData.entrySet().stream()
                .map(entry -> {
                    Company company = entry.getKey();
                    Map<MemberProjectRole, List<Member>> roleToMembersMap = entry.getValue();

                    // 역할별 멤버 이름 추출
                    List<String> managerNames = extractMemberNames(roleToMembersMap.get(MemberProjectRole.CLI_MANAGER));
                    List<String> memberNames = extractMemberNames(roleToMembersMap.get(MemberProjectRole.CLI_PARTICIPANT));

                    // ClientAssignmentInfo 생성
                    return ClientAssignmentInfo.builder()
                            .companyName(company.getName())
                            .managerNames(managerNames)
                            .memberNames(memberNames)
                            .build();
                })
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