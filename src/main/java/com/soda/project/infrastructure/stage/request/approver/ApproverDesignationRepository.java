package com.soda.project.infrastructure.stage.request.approver;

import com.soda.project.domain.stage.request.approver.ApproverDesignation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApproverDesignationRepository extends JpaRepository<ApproverDesignation, Long> {
}
