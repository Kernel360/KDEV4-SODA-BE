package com.soda.project.domain.stage.request.approver;

import java.util.Optional;

public interface ApproverDesignationProvider {
    Optional<ApproverDesignation> findById(Long approverId);
}
