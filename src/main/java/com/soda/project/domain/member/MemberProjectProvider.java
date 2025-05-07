package com.soda.project.domain.member;

import com.soda.member.domain.Company;
import com.soda.member.domain.Member;
import com.soda.project.domain.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface MemberProjectProvider {
    List<MemberProject> findAllByProjectAndMember_CompanyIdAndIsDeletedFalse(Project project, Long companyId);

    Optional<MemberProject> findByProjectAndMemberIdAndIsDeletedFalse(Project project, Long memberId);

    MemberProject store(MemberProject memberProject);

    Optional<MemberProject> findByMemberAndProject(Member member, Project project);

    Page<MemberProject> findByMemberId(Long userId, Pageable pageable);

    List<MemberProject> findAllByProjectAndMember_CompanyAndRoleAndIsDeletedFalse(Project project, Company company, MemberProjectRole role);

    List<MemberProject> findByProjectAndRoleAndIsDeletedFalse(Project project, MemberProjectRole role);

    boolean existsByMemberAndProjectAndIsDeletedFalse(Member member, Project project);

    List<MemberProject> findByProject(Project project);

    List<MemberProject> saveAll(List<MemberProject> memberProjects);

    Optional<MemberProject> findByMemberAndProjectAndIsDeletedFalse(Member member, Project project);

    Page<MemberProject> findFilteredMembers(Long projectId, List<Long> filteredCompanyIds, Long specificCompanyId, MemberProjectRole memberRole, Long memberId, Pageable pageable);

    List<Long> findAllProjectIdsByMemberIdAndIsDeletedFalse(Long memberId);
}
