package com.soda.project.repository;

import com.soda.member.entity.Company;
import com.soda.member.entity.Member;
import com.soda.member.enums.MemberProjectRole;
import com.soda.project.entity.MemberProject;
import com.soda.project.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberProjectRepository extends JpaRepository<MemberProject, Long> {

    List<MemberProject> findByProject(Project project);

    boolean existsByMemberAndProjectAndIsDeletedFalse(Member member, Project project);

    List<MemberProject> findByProjectAndRoleAndIsDeletedFalse(Project project, MemberProjectRole role);

    Optional<MemberProject> findByMemberAndProject(Member member, Project project);

    Page<MemberProject> findByMemberId(Long userId, Pageable pageable);

    Optional<MemberProject> findByMemberAndProjectAndIsDeletedFalse(Member member, Project project);

    List<MemberProject> findAllByProjectAndMember_CompanyIdAndIsDeletedFalse(Project project, Long companyId);

    Optional<MemberProject> findByProjectAndMemberIdAndIsDeletedFalse(Project project, Long memberId);

    List<MemberProject> findAllByProjectAndMember_CompanyAndRoleAndIsDeletedFalse(
            Project project,
            Company company,
            MemberProjectRole role
    );}
