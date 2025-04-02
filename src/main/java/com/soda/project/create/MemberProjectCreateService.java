package com.soda.project.create;

import com.soda.global.response.GeneralException;
import com.soda.member.Company;
import com.soda.member.Member;
import com.soda.member.MemberProjectRole;
import com.soda.project.MemberProject;
import com.soda.project.MemberProjectRepository;
import com.soda.project.Project;
import com.soda.project.ProjectErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberProjectCreateService {
    private final MemberProjectRepository memberProjectRepository;

    public void assignMembersToProject(Company company, List<Member> members, Project project, MemberProjectRole role) {
        members.forEach(member -> {
            if (!member.getCompany().getId().equals(company.getId())) {
                throw new GeneralException(ProjectErrorCode.INVALID_MEMBER_COMPANY);
            }

            if (!existsByMemberAndProjectAndIsDeletedFalse(member, project)) {
                createAndSaveMemberProject(member, project, role);
            }
        });
    }

    private void createAndSaveMemberProject(Member member, Project project, MemberProjectRole role) {
        MemberProject memberProject = MemberProject.builder()
                .member(member)
                .project(project)
                .memberProjectRole(role)
                .build();
        memberProjectRepository.save(memberProject);
    }

    public boolean existsByMemberAndProjectAndIsDeletedFalse(Member member, Project project) {
        return memberProjectRepository.existsByMemberAndProjectAndIsDeletedFalse(member, project);
    }
}
