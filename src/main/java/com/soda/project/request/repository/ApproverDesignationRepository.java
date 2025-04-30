package com.soda.project.request.repository;

import com.soda.project.domain.stage.request.ApproverDesignation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApproverDesignationRepository extends JpaRepository<ApproverDesignation, Long> {
}
