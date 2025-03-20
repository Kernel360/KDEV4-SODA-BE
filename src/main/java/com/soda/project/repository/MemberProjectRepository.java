package com.soda.project.repository;

import com.soda.project.entity.MemberProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberProjectRepository extends JpaRepository<MemberProject, Long> {
}
