package com.soda.project.member;

import com.soda.member.Member;
import com.soda.member.MemberProjectRole;
import com.soda.project.Project;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemberProjectRepository extends JpaRepository<MemberProject, Long> {

    boolean existsByMemberAndProjectAndIsDeletedFalse(Member member, Project project);

    List<MemberProject> findByProjectAndRoleAndIsDeletedFalse(Project project, MemberProjectRole role);
}
