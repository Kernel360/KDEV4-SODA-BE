package com.soda.project.domain.member;

import com.soda.global.response.GeneralException;
import com.soda.member.entity.Company;
import com.soda.member.entity.Member;
import com.soda.project.domain.Project;
import com.soda.project.domain.ProjectErrorCode;
import com.soda.project.domain.company.CompanyProjectRole;
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
    private final MemberProjectProvider memberProjectProvider;

    public void addOrUpdateProjectMembers(Project project, CompanyProjectRole companyRole, List<Member> managers, List<Member> members) {
        log.info("프로젝트 멤버 추가/업데이트 시작: projectId={}, companyRole={}", project.getId(), companyRole);

        // 1. 역할 결정 (전달받은 companyRole 사용)
        MemberProjectRole targetManagerRole = determineTargetManagerRole(companyRole);
        MemberProjectRole targetMemberRole = determineTargetMemberRole(companyRole);
        log.debug("결정된 멤버 역할: manager={}, member={}", targetManagerRole, targetMemberRole);

        // 2. 매니저 할당/업데이트
        if (!CollectionUtils.isEmpty(managers)) {
            assignOrUpdateInternal(managers, project, targetManagerRole);
        }
        // 3. 멤버 할당/업데이트
        if (!CollectionUtils.isEmpty(members)) {
            assignOrUpdateInternal(members, project, targetMemberRole);
        }

        log.info("프로젝트 멤버 추가/업데이트 완료: projectId={}, companyRole={}", project.getId(), companyRole);
    }

    private MemberProjectRole determineTargetMemberRole(CompanyProjectRole companyRole) {
        if (companyRole == CompanyProjectRole.DEV_COMPANY) return MemberProjectRole.DEV_PARTICIPANT;
        if (companyRole == CompanyProjectRole.CLIENT_COMPANY) return MemberProjectRole.CLI_PARTICIPANT;
        throw new GeneralException(ProjectErrorCode.COMPANY_PROJECT_NOT_FOUND); // 도달 불가능 로직이나 방어 코드
    }

    private MemberProjectRole determineTargetManagerRole(CompanyProjectRole companyRole) {
        if (companyRole == CompanyProjectRole.DEV_COMPANY) return MemberProjectRole.DEV_MANAGER;
        if (companyRole == CompanyProjectRole.CLIENT_COMPANY) return MemberProjectRole.CLI_MANAGER;
        throw new GeneralException(ProjectErrorCode.COMPANY_PROJECT_NOT_FOUND);
    }

    private void assignOrUpdateInternal(List<Member> members, Project project, MemberProjectRole role) {
        if (CollectionUtils.isEmpty(members)) return;

        members.forEach(member -> {
            Optional<MemberProject> existingEntryOpt = memberProjectProvider.findByMemberAndProject(member, project);

            if (existingEntryOpt.isPresent()) {
                updateExistingMemberProject(existingEntryOpt.get(), role);
            } else {
                MemberProject newMemberProject = MemberProject.create(member, project, role);
                memberProjectProvider.store(newMemberProject);
            }
        });
    }

    private void updateExistingMemberProject(MemberProject existingEntry, MemberProjectRole newRole) {
        boolean changed = false;
        // 삭제된 상태면 재활성화
        if (existingEntry.getIsDeleted()) {
            existingEntry.reActive();
            log.info("MemberProject 재활성화: memberProjectId={}", existingEntry.getId());
            changed = true;
        }
        // 역할이 다르면 변경
        if (existingEntry.getRole() != newRole) {
            log.info("MemberProject 역할 변경: memberProjectId={}, oldRole={}, newRole={}",
                    existingEntry.getId(), existingEntry.getRole(), newRole);
            existingEntry.changeRole(newRole);
            changed = true;
        }
        // 변경 사항 있으면 저장 (Provider 사용)
        if (changed) {
            memberProjectProvider.store(existingEntry);
        } else {
            log.debug("MemberProject 변경사항 없음: memberProjectId={}", existingEntry.getId());
        }
    }

    public List<Member> getMembersByRole(Project project, MemberProjectRole role) {
        return memberProjectProvider.findByProjectAndRoleAndIsDeletedFalse(project, role).stream()
                .map(MemberProject::getMember)
                .collect(Collectors.toList());
    }

    public boolean existsByMemberAndProjectAndIsDeletedFalse(Member member, Project project) {
        return memberProjectProvider.existsByMemberAndProjectAndIsDeletedFalse(member, project);
    }

    public List<Member> getMembersByCompanyAndRole(Project project, Company company, MemberProjectRole role) {
        log.debug("프로젝트 내 회사/역할별 멤버 조회 시작: projectId={}, companyId={}, role={}",
                project.getId(), company.getId(), role);

        List<MemberProject> memberProjects = memberProjectProvider
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

    /**
     * 특정 프로젝트에서 특정 멤버의 역할을 조회합니다.
     * 멤버가 해당 프로젝트에 참여하지 않거나, 참여 정보가 삭제된 경우 null을 반환합니다.
     */
    public MemberProjectRole getMemberRoleInProject(Member member, Project project) {
        Optional<MemberProject> memberProjectOpt = memberProjectProvider.findByMemberAndProjectAndIsDeletedFalse(member, project);
        return memberProjectOpt.map(MemberProject::getRole).orElse(null);
    }

    @Transactional
    public void deleteMembersFromProject(Project project, Long companyId) {
        List<MemberProject> membersToDelete = memberProjectProvider
                .findAllByProjectAndMember_CompanyIdAndIsDeletedFalse(project, companyId);
        
        for (MemberProject memberProject: membersToDelete) {
            memberProject.delete();
        }

        log.info("멤버 {}명이 프로젝트 ID {} 에서 삭제되었습니다.", membersToDelete.size(), project.getId());
    }

    @Transactional
    public void deleteSingleMemberFromProject(Project project, Long memberId) {
        MemberProject memberToDelete = memberProjectProvider.findByProjectAndMemberIdAndIsDeletedFalse(project, memberId)
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
        return memberProjectProvider.findFilteredMembers(
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
        List<Long> projectIds = memberProjectProvider.findAllProjectIdsByMemberIdAndIsDeletedFalse(memberId);
        log.info("회원 ID {}가 참여하는 프로젝트 ID 목록 조회 완료: {}개", memberId, projectIds.size());
        return projectIds;
    }
}