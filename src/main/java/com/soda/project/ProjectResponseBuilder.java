package com.soda.project;

import com.soda.member.entity.Company;
import com.soda.member.entity.Member;
import com.soda.project.domain.Project;
import com.soda.project.domain.member.MemberProjectService;
import com.soda.project.domain.member.MemberProjectRole;
import com.soda.project.interfaces.dto.CompanyAssignment;
import com.soda.project.interfaces.dto.DevCompanyAssignmentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProjectResponseBuilder {
    private final MemberProjectService memberProjectService;

    public DevCompanyAssignmentResponse createDevCompanyAssignmentResponse(Project project, List<Company> assignedCompanies) {

        if (CollectionUtils.isEmpty(assignedCompanies)) {
            return DevCompanyAssignmentResponse.from(Collections.emptyList()); // 빈 리스트로 from 호출
        }

        // 각 개발사별 멤버 ID 조회 및 CompanyAssignment DTO 리스트 생성
        List<CompanyAssignment> devAssignmentsForResponse = assignedCompanies.stream()
                .map(company -> {
                    // 해당 회사의 개발사 매니저/참여자 조회 (MemberProjectService 사용)
                    List<Member> managers = memberProjectService.getMembersByCompanyAndRole(project, company, MemberProjectRole.DEV_MANAGER);
                    List<Member> members = memberProjectService.getMembersByCompanyAndRole(project, company, MemberProjectRole.DEV_PARTICIPANT);

                    // 멤버 ID 추출
                    List<Long> managerIds = extractMemberIds(managers);
                    List<Long> memberIds = extractMemberIds(members);

                    // CompanyAssignment DTO 생성
                    return CompanyAssignment.builder()
                            .companyId(company.getId())
                            .managerIds(managerIds)
                            .memberIds(memberIds)
                            .build();
                })
                .collect(Collectors.toList());

        return DevCompanyAssignmentResponse.from(devAssignmentsForResponse);
    }

    // 멤버 ID 추출 헬퍼 메서드
    private List<Long> extractMemberIds(List<Member> members) {
        if (members == null || members.isEmpty()) {
            return Collections.emptyList();
        }
        return members.stream()
                .map(Member::getId)
                .collect(Collectors.toList());
    }
}
