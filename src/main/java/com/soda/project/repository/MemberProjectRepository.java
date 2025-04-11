package com.soda.project.repository;

import com.soda.member.entity.Member;
import com.soda.member.enums.MemberProjectRole;
import com.soda.project.entity.MemberProject;
import com.soda.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
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

    List<MemberProject> findByMemberId(Long userId);
}
