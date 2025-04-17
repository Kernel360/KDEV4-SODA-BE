package com.soda.request.service;

import com.soda.global.response.GeneralException;
import com.soda.request.dto.request.ApproverDeleteResponse;
import com.soda.request.entity.ApproverDesignation;
import com.soda.request.error.ApproverDesignationErrorCode;
import com.soda.request.error.RequestErrorCode;
import com.soda.request.repository.ApproverDesignationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ApproverDesignationService {
    private final ApproverDesignationRepository approverDesignationRepository;

    @Transactional
    public ApproverDeleteResponse deleteApprover(Long approverId, Long memberId) {
        ApproverDesignation approver = getApproveDesignationOrThrow(approverId);

        // 현재 유저가 승인 생성자이닞 확인
        validateRequestWriter(memberId, approver);

        // request 소프트 삭제
        approver.delete();

        return ApproverDeleteResponse.fromEntity(approver);
    }

    private void validateRequestWriter(Long memberId, ApproverDesignation approver) {
        if(!approver.getRequest().getMember().getId().equals(memberId)) {
            throw new GeneralException(RequestErrorCode.USER_NOT_WRITE_REQUEST);
        }
    }

    private ApproverDesignation getApproveDesignationOrThrow(Long approverId) {
        return approverDesignationRepository.findById(approverId)
                .orElseThrow(() -> new GeneralException(ApproverDesignationErrorCode.APPROVE_NOT_FOUND));
    }
}
