package com.soda.project.infrastructure;

import com.soda.member.entity.Company;
import com.soda.member.entity.Member;
import com.soda.project.domain.Project;
import com.soda.project.domain.member.MemberProject;
import com.soda.project.domain.member.MemberProjectProvider;
import com.soda.project.domain.member.MemberProjectRole;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MemberProjectProviderImpl implements MemberProjectProvider {
    private final MemberProjectRepository memberProjectRepository;

    @Override
    public List<MemberProject> findAllByProjectAndMember_CompanyIdAndIsDeletedFalse(Project project, Long companyId) {
        return memberProjectRepository.findAllByProjectAndMember_CompanyIdAndIsDeletedFalse(project, companyId);
    }

    @Override
    public Optional<MemberProject> findByProjectAndMemberIdAndIsDeletedFalse(Project project, Long memberId) {
        return memberProjectRepository.findByProjectAndMemberIdAndIsDeletedFalse(project, memberId);
    }

    @Override
    public MemberProject store(MemberProject memberProject) {
        return memberProjectRepository.save(memberProject);
    }

    @Override
    public Optional<MemberProject> findByMemberAndProject(Member member, Project project) {
        return memberProjectRepository.findByMemberAndProject(member, project);
    }

    @Override
    public Page<MemberProject> findByMemberId(Long userId, Pageable pageable) {
        return memberProjectRepository.findByMemberId(userId, pageable);
    }

    @Override
    public List<MemberProject> findAllByProjectAndMember_CompanyAndRoleAndIsDeletedFalse(Project project, Company company, MemberProjectRole role) {
        return memberProjectRepository.findAllByProjectAndMember_CompanyAndRoleAndIsDeletedFalse(project, company, role);
    }

    @Override
    public List<MemberProject> findByProjectAndRoleAndIsDeletedFalse(Project project, MemberProjectRole role) {
        return memberProjectRepository.findByProjectAndRoleAndIsDeletedFalse(project, role);
    }

    @Override
    public boolean existsByMemberAndProjectAndIsDeletedFalse(Member member, Project project) {
        return memberProjectRepository.existsByMemberAndProjectAndIsDeletedFalse(member, project);
    }

    @Override
    public List<MemberProject> findByProject(Project project) {
        return memberProjectRepository.findByProject(project);
    }

    @Override
    public List<MemberProject> saveAll(List<MemberProject> memberProjects) {
        return memberProjectRepository.saveAll(memberProjects);
    }

    @Override
    public Optional<MemberProject> findByMemberAndProjectAndIsDeletedFalse(Member member, Project project) {
        return memberProjectRepository.findByMemberAndProjectAndIsDeletedFalse(member, project);
    }

    @Override
    public Page<MemberProject> findFilteredMembers(Long projectId, List<Long> filteredCompanyIds, Long companyId, MemberProjectRole memberRole, Long memberId, Pageable pageable) {
        return memberProjectRepository.findFilteredMembers(projectId, filteredCompanyIds,
                companyId,
                memberRole,
                memberId,
                pageable);
    }

    @Override
    public List<Long> findAllProjectIdsByMemberIdAndIsDeletedFalse(Long memberId) {
        return memberProjectRepository.findAllProjectIdsByMemberIdAndIsDeletedFalse(memberId);
    }
}
