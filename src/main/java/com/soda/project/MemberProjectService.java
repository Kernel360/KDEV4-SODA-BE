package com.soda.project;

import com.soda.global.response.GeneralException;
import com.soda.member.Company;
import com.soda.member.Member;
import com.soda.member.MemberProjectRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberProjectService {

    private final MemberProjectRepository memberProjectRepository;

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

    // 멤버 추가 및 수정 메서드
    public void addOrUpdateMembersInProject(Project project, List<Member> members, MemberProjectRole role) {
        // 각 멤버에 대해 처리
        for (Member member : members) {
            // 멤버가 해당 프로젝트에 이미 있는지 확인
            MemberProject existingMemberProject = memberProjectRepository.findByMemberAndProject(member, project)
                    .orElseThrow(() -> new GeneralException(ProjectErrorCode.MEMBER_NOT_IN_PROJECT));

            if (existingMemberProject != null) {
                // 멤버가 이미 존재하면, 삭제되지 않은 상태에서만 역할을 업데이트
                if (!existingMemberProject.getIsDeleted()) {
                    // 역할 업데이트 (멤버가 해당 프로젝트에 이미 있는 경우)
                    existingMemberProject.updateMemberProject(member, project, role);
                    memberProjectRepository.save(existingMemberProject);
                } else {
                    // 멤버가 삭제된 상태라면 복구 후 역할 업데이트
                    existingMemberProject.reActive();  // isDeleted = false
                    existingMemberProject.updateMemberProject(member, project, role);
                    memberProjectRepository.save(existingMemberProject);
                }
            } else {
                // 멤버가 존재하지 않으면 새로 추가
                createAndSaveMemberProject(member, project, role);
            }
        }
    }
}
