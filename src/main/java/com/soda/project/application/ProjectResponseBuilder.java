package com.soda.project.application;

import com.querydsl.core.Tuple;
import com.soda.member.entity.Company;
import com.soda.member.entity.Member;
import com.soda.project.domain.Project;
import com.soda.project.domain.company.CompanyProjectRole;
import com.soda.project.domain.company.CompanyProjectService;
import com.soda.project.domain.member.MemberProjectRole;
import com.soda.project.domain.member.MemberProjectService;
import com.soda.project.interfaces.dto.CompanyAssignment;
import com.soda.project.interfaces.dto.DevCompanyAssignmentResponse;
import com.soda.project.interfaces.dto.MyProjectListResponse;
import com.soda.project.interfaces.dto.ProjectViewResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProjectResponseBuilder {
    private static final String ADMIN_ROLE = "ADMIN";
    private static final String USER_ROLE = "USER";

    private final MemberProjectService memberProjectService;
    private final CompanyProjectService companyProjectService;

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

    public Page<MyProjectListResponse> createMyProjectListResponsePage(Page<Tuple> tuplePage, boolean isMemberRoleRequired) {
        if (tuplePage.isEmpty()) {
            return Page.empty(tuplePage.getPageable());
        }
        return tuplePage.map(tuple -> mapTupleToMyProjectListResponse(tuple, isMemberRoleRequired));
    }

    private MyProjectListResponse mapTupleToMyProjectListResponse(Tuple tuple, boolean isMemberRoleRequired) {
        Project project = tuple.get(0, Project.class);
        CompanyProjectRole companyRole = tuple.get(1, CompanyProjectRole.class);
        MemberProjectRole memberRole = tuple.get(2, MemberProjectRole.class);
        return MyProjectListResponse.from(project, companyRole, memberRole);
    }

    public ProjectViewResponse createProjectViewResponse(Project project, Member member, String userRole) {
        // 회사 이름 조회
        List<String> devCompanyNames = companyProjectService.getCompanyNamesByRole(project, CompanyProjectRole.DEV_COMPANY);
        List<String> clientCompanyNames = companyProjectService.getCompanyNamesByRole(project, CompanyProjectRole.CLIENT_COMPANY);

        // 현재 사용자의 프로젝트 내 역할 조회 (!!! userRole 전달 !!!)
        String currentMemberProjectRole = determineMemberProjectRole(member, project, userRole);
        String currentCompanyProjectRole = determineCompanyProjectRole(member, project, userRole);

        // 역할별 멤버 목록 조회
        List<Member> devManagers = memberProjectService.getMembersByRole(project, MemberProjectRole.DEV_MANAGER);
        List<Member> devMembers = memberProjectService.getMembersByRole(project, MemberProjectRole.DEV_PARTICIPANT);
        List<Member> clientManagers = memberProjectService.getMembersByRole(project, MemberProjectRole.CLI_MANAGER);
        List<Member> clientMembers = memberProjectService.getMembersByRole(project, MemberProjectRole.CLI_PARTICIPANT);

        // 이름 추출 (헬퍼 메서드 사용)
        List<String> devManagerNames = extractMemberNames(devManagers);
        List<String> devMemberNames = extractMemberNames(devMembers);
        List<String> clientManagerNames = extractMemberNames(clientManagers);
        List<String> clientMemberNames = extractMemberNames(clientMembers);

        return ProjectViewResponse.from(
                project, currentMemberProjectRole, currentCompanyProjectRole,
                clientCompanyNames, clientManagerNames, clientMemberNames,
                devCompanyNames, devManagerNames, devMemberNames
        );
    }

    /** 멤버 리스트에서 이름 리스트 추출 */
    private List<String> extractMemberNames(List<Member> members) {
        if (members == null || members.isEmpty()) {
            return Collections.emptyList(); // null 대신 빈 리스트
        }
        return members.stream()
                .map(Member::getName)
                .collect(Collectors.toList());
    }

    /** 현재 사용자의 프로젝트 내 멤버 역할 결정 */
    private String determineMemberProjectRole(Member member, Project project, String userRole) {
        if (USER_ROLE.equals(userRole)) {
            MemberProjectRole role = memberProjectService.getMemberRoleInProject(member, project);
            return (role != null) ? role.getDescription() : "Unknown Role";
        } else if (ADMIN_ROLE.equals(userRole)) {
            return ADMIN_ROLE;
        }
        return "Unknown Role";
    }

    /** 현재 사용자의 프로젝트 내 회사 역할 결정 */
    private String determineCompanyProjectRole(Member member, Project project, String userRole) {
        if (USER_ROLE.equals(userRole)) {
            if (member == null || member.getCompany() == null) {
                return "No Company Info";
            }
            CompanyProjectRole role = companyProjectService.getCompanyRoleInProject(member.getCompany(), project);
            return (role != null) ? role.getDescription() : "Unknown Role";
        } else if (ADMIN_ROLE.equals(userRole)) {
            return ADMIN_ROLE;
        }
        return "Unknown Role";
    }
}