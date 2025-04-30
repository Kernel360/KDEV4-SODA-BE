package com.soda.project.interfaces.dto;

import com.soda.member.entity.Company;
import com.soda.member.entity.Member;
import com.soda.member.enums.MemberProjectRole;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Builder
public class DevCompanyAssignmentResponse {

    // 개발사별 할당 정보 리스트 (CompanyAssignment 직접 사용)
    private List<CompanyAssignment> devAssignments;

    public static DevCompanyAssignmentResponse from(
            Map<Company, Map<MemberProjectRole, List<Member>>> devAssignmentData) {

        // Map 데이터를 List<CompanyAssignment>로 변환
        List<CompanyAssignment> assignments = devAssignmentData.entrySet().stream()
                .map(entry -> {
                    Company company = entry.getKey();
                    Map<MemberProjectRole, List<Member>> roleToMembersMap = entry.getValue();

                    // 역할별 멤버 id 추출
                    List<Long> managerIds = extractMemberIds(roleToMembersMap.get(MemberProjectRole.DEV_MANAGER));
                    List<Long> memberIds = extractMemberIds(roleToMembersMap.get(MemberProjectRole.DEV_PARTICIPANT));

                    // CompanyAssignment 생성
                    return CompanyAssignment.builder()
                            .companyId(company.getId())
                            .managerIds(managerIds)
                            .memberIds(memberIds)
                            .build();
                })
                .collect(Collectors.toList());

        // 최종 응답 DTO 빌드
        return DevCompanyAssignmentResponse.builder()
                .devAssignments(assignments)
                .build();
    }

    // 멤버 ID 추출 헬퍼 메서드
    private static List<Long> extractMemberIds(List<Member> members) {
        if (members == null || members.isEmpty()) {
            return List.of();
        }
        return members.stream()
                .map(Member::getId)
                .collect(Collectors.toList());
    }
}