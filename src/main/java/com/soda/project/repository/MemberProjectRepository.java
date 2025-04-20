package com.soda.project.repository;

import com.soda.member.entity.Company;
import com.soda.member.entity.Member;
import com.soda.member.enums.MemberProjectRole;
import com.soda.project.entity.MemberProject;
import com.soda.project.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberProjectRepository extends JpaRepository<MemberProject, Long> {

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

    @Query(value = "SELECT mp FROM MemberProject mp JOIN FETCH mp.member m JOIN FETCH m.company c " +
            "WHERE mp.project.id = :projectId AND mp.isDeleted = false " +
            "AND (:companyIds IS NULL OR c.id IN :companyIds) " +
            "AND (:companyId IS NULL OR c.id = :companyId) " +
            "AND (:memberRole IS NULL OR mp.role = :memberRole)",
            countQuery = "SELECT COUNT(mp) FROM MemberProject mp JOIN mp.member m JOIN m.company c " +
                    "WHERE mp.project.id = :projectId AND mp.isDeleted = false " +
                    "AND (:companyIds IS NULL OR c.id IN :companyIds) " +
                    "AND (:companyId IS NULL OR c.id = :companyId) " +
                    "AND (:memberRole IS NULL OR mp.role = :memberRole)")
    Page<MemberProject> findFilteredMembersAndIsDeletedFalse(
                                                              @Param("projectId") Long projectId,
                                                              @Param("companyIds") List<Long> companyIds,
                                                              @Param("companyId") Long companyId,
                                                              @Param("memberRole") MemberProjectRole memberRole,
                                                              Pageable pageable);

}
