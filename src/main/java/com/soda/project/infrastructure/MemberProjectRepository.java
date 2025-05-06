package com.soda.project.infrastructure;

import com.soda.member.domain.Company;
import com.soda.member.domain.Member;
import com.soda.project.domain.member.enums.MemberProjectRole;
import com.soda.project.domain.member.MemberProject;
import com.soda.project.domain.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberProjectRepository extends JpaRepository<MemberProject, Long>, MemberProjectRepositoryCustom {

    List<MemberProject> findByProject(Project project);

    boolean existsByMemberAndProjectAndIsDeletedFalse(Member member, Project project);

    List<MemberProject> findByProjectAndRoleAndIsDeletedFalse(Project project, MemberProjectRole role);

    Page<MemberProject> findByMemberId(Long userId, Pageable pageable);

    Optional<MemberProject> findByMemberAndProjectAndIsDeletedFalse(Member member, Project project);

    List<MemberProject> findAllByProjectAndMember_CompanyIdAndIsDeletedFalse(Project project, Long companyId);

    Optional<MemberProject> findByProjectAndMemberIdAndIsDeletedFalse(Project project, Long memberId);

    List<MemberProject> findAllByProjectAndMember_CompanyAndRoleAndIsDeletedFalse(
            Project project,
            Company company,
            MemberProjectRole role
    );

    @Query("SELECT mp.project.id FROM MemberProject mp WHERE mp.member.id = :memberId AND mp.isDeleted = false")
    List<Long> findAllProjectIdsByMemberIdAndIsDeletedFalse(@Param("memberId") Long memberId);

    Optional<MemberProject> findByMemberAndProject(Member member, Project project);
}
