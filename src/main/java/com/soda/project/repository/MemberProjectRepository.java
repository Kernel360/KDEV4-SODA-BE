package com.soda.project.repository;

import com.soda.member.enums.MemberProjectRole;
import com.soda.project.entity.MemberProject;
import com.soda.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

@Repository
public interface MemberProjectRepository extends JpaRepository<MemberProject, Long> {
    List<MemberProject> findByProjectAndRole(Project project, MemberProjectRole memberProjectRole);
}
