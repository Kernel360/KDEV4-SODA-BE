package com.soda.project.infrastructure.stage.request.approver;

import com.soda.project.domain.stage.request.approver.ApproverDesignation;
import com.soda.project.domain.stage.request.approver.ApproverDesignationProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ApproverDesignationProviderImpl implements ApproverDesignationProvider {

    private final ApproverDesignationRepository approverDesignationRepository;

    @Override
    public Optional<ApproverDesignation> findById(Long approverId) {
        return approverDesignationRepository.findById(approverId);
    }
}
