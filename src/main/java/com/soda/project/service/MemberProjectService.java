package com.soda.project.service;

import com.soda.member.entity.Member;
import com.soda.member.enums.MemberProjectRole;
import com.soda.project.entity.MemberProject;
import com.soda.project.entity.Project;
import com.soda.project.repository.MemberProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberProjectService {
    private final MemberProjectRepository memberProjectRepository;

    public boolean existsByMemberAndProjectAndIsDeletedFalse(Member member, Project project) {
        return memberProjectRepository.existsByMemberAndProjectAndIsDeletedFalse(member, project);
    }

    public void save(MemberProject memberProject) {
        memberProjectRepository.save(memberProject);  // 새로운 멤버를 프로젝트에 추가
    }

    public List<Member> getMembersByRole(Project project, MemberProjectRole role) {
        return memberProjectRepository.findByProjectAndRoleAndIsDeletedFalse(project, role).stream()
                .map(MemberProject::getMember)
                .collect(Collectors.toList());
    }

    public List<MemberProject> findByProjectAndRole(Project project, MemberProjectRole role) {
        return memberProjectRepository.findByProjectAndRole(project, role);
    }

    public void saveAll(List<MemberProject> memberProjects) {
        memberProjectRepository.saveAll(memberProjects);
    }

    public MemberProject findByMemberAndProjectAndRole(Member member, Project project, MemberProjectRole role) {
        return memberProjectRepository.findByMemberAndProjectAndRole(member, project, role);
    }

    public List<MemberProject> findByProject(Project project) {
        return memberProjectRepository.findByProject(project);
    }
    
    public MemberProject createMemberProject(Member member, Project project, MemberProjectRole role) {
        return MemberProject.builder()
                .member(member)
                .project(project)
                .memberProjectRole(role)
                .build();
    }
}
