package com.soda.project.service;

import com.soda.global.response.GeneralException;
import com.soda.member.entity.Company;
import com.soda.member.entity.Member;
import com.soda.member.enums.MemberProjectRole;
import com.soda.project.entity.MemberProject;
import com.soda.project.entity.Project;
import com.soda.project.error.ProjectErrorCode;
import com.soda.project.repository.MemberProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberProjectService {

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

    public List<Member> getMembersByRole(Project project, MemberProjectRole role) {
        return memberProjectRepository.findByProjectAndRoleAndIsDeletedFalse(project, role).stream()
                .map(MemberProject::getMember)
                .collect(Collectors.toList());
    }

    public boolean existsByMemberAndProjectAndIsDeletedFalse(Member member, Project project) {
        return memberProjectRepository.existsByMemberAndProjectAndIsDeletedFalse(member, project);
    }

    public void deleteMemberProjects(Project project) {
        List<MemberProject> memberProjects = memberProjectRepository.findByProject(project);
        memberProjects.forEach(MemberProject::delete);
        memberProjectRepository.saveAll(memberProjects);
    }
}
