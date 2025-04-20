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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Transactional(readOnly = true)
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

    public List<Member> getMembersByCompanyAndRole(Project project, Company company, MemberProjectRole role) {
        log.debug("프로젝트 내 회사/역할별 멤버 조회 시작: projectId={}, companyId={}, role={}",
                project.getId(), company.getId(), role);

        List<MemberProject> memberProjects = memberProjectRepository
                .findAllByProjectAndMember_CompanyAndRoleAndIsDeletedFalse(project, company, role);

        if (CollectionUtils.isEmpty(memberProjects)) {
            log.debug("해당 조건의 멤버 없음.");
            return List.of(); // 빈 리스트 반환
        }

        // MemberProject 리스트에서 Member 엔티티만 추출하여 반환
        List<Member> members = memberProjects.stream()
                .map(MemberProject::getMember)
                .distinct()
                .collect(Collectors.toList());

        log.debug("멤버 조회 완료: count={}", members.size());
        return members;
    }

    // 사용자가 참여한 프로젝트 리스트 조회
    public Page<Long> getProjectIdsByUserId(Long userId, Pageable pageable) {
        log.info("사용자가 참여한 프로젝트 ID 목록 조회 시작: 사용자 ID = {}", userId);

        Page<MemberProject> memberProjects = memberProjectRepository.findByMemberId(userId, pageable);
        log.info("사용자 ID = {}가 참여한 프로젝트 ID 목록 조회 완료: 조회된 프로젝트 수 = {}", userId, memberProjects.getSize());

        return memberProjects.map(memberProject -> memberProject.getProject().getId());
    }

    /**
     * 특정 프로젝트에서 특정 멤버의 역할을 조회합니다.
     * 멤버가 해당 프로젝트에 참여하지 않거나, 참여 정보가 삭제된 경우 null을 반환합니다.
     *
     * @param project 조회할 프로젝트 엔티티
     * @param member  역할을 조회할 멤버 엔티티
     * @return 해당 프로젝트에서의 MemberProjectRole, 참여하지 않거나 삭제된 경우 null
     */
    public MemberProjectRole getMemberRoleInProject(Member member, Project project) {
        Optional<MemberProject> memberProjectOpt = memberProjectRepository.findByMemberAndProjectAndIsDeletedFalse(member, project);
        return memberProjectOpt.map(MemberProject::getRole).orElse(null);
    }

    @Transactional
    public void deleteMembersFromProject(Project project, Long companyId) {
        List<MemberProject> membersToDelete = memberProjectRepository
                .findAllByProjectAndMember_CompanyIdAndIsDeletedFalse(project, companyId);
        
        for (MemberProject memberProject: membersToDelete) {
            memberProject.delete();
        }

        log.info("멤버 {}명이 프로젝트 ID {} 에서 삭제되었습니다.", membersToDelete.size(), project.getId());
    }

    @Transactional
    public void deleteSingleMemberFromProject(Project project, Long memberId) {
        MemberProject memberToDelete = memberProjectRepository.findByProjectAndMemberIdAndIsDeletedFalse(project, memberId)
                .orElseThrow(() -> new GeneralException(ProjectErrorCode.MEMBER_PROJECT_NOT_FOUND));

        memberToDelete.delete();
        log.info("단일 멤버 연결 삭제 완료: projectId={}, memberId={}, memberProjectId={}",
                project.getId(), memberId, memberToDelete.getId());
    }

    public Page<MemberProject> getFilteredMemberProjectsAndIsDeletedFalse(
                                                                           Long projectId,
                                                                           List<Long> filteredCompanyIds,
                                                                           Long specificCompanyId,
                                                                           MemberProjectRole memberRole,
                                                                           Long memberId,
                                                                           Pageable pageable) {

        log.debug("삭제되지 않은 MemberProject 필터링 조회 (Repository 호출): projectId={}, filteredCompanyIds={}, specificCompanyId={}, memberRole={}",
                projectId, filteredCompanyIds, specificCompanyId, memberRole);

        // 수정된 Repository 메서드 호출
        return memberProjectRepository.findFilteredMembers(
                projectId,
                filteredCompanyIds,
                specificCompanyId,
                memberRole,
                memberId,
                pageable
        );
    }

    public List<Long> findProjectIdsByMemberId(Long memberId) {
        log.info("회원 ID {}가 참여하는 프로젝트 ID 목록 조회 시작", memberId);
        List<Long> projectIds = memberProjectRepository.findAllProjectIdsByMemberIdAndIsDeletedFalse(memberId);
        log.info("회원 ID {}가 참여하는 프로젝트 ID 목록 조회 완료: {}개", memberId, projectIds.size());
        return projectIds;
    }
}