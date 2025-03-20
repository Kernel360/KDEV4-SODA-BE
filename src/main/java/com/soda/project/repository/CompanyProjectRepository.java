package com.soda.project.repository;

import com.soda.project.entity.CompanyProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyProjectRepository extends JpaRepository<CompanyProject, Long> {
}
