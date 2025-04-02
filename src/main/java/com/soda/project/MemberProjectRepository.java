package com.soda.project;

import com.soda.member.Member;
import com.soda.member.MemberProjectRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberProjectRepository extends JpaRepository<MemberProject, Long> {

    List<MemberProject> findByProject(Project project);

    boolean existsByMemberAndProjectAndIsDeletedFalse(Member member, Project project);

    List<MemberProject> findByProjectAndRoleAndIsDeletedFalse(Project project, MemberProjectRole role);

    List<MemberProject> findByProjectAndRole(Project project, MemberProjectRole role);

    MemberProject findByMemberAndProjectAndRole(Member member, Project project, MemberProjectRole role);

    List<Project> findByMemberIdAndIsDeletedFalse(Long memberId);

    Optional<MemberProject> findByMemberAndProject(Member member, Project project);
}
