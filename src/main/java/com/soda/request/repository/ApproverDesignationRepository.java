package com.soda.request.repository;

import com.soda.request.entity.ApproverDesignation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApproverDesignationRepository extends JpaRepository<ApproverDesignation, Long> {
}
