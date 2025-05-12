package com.soda.project.interfaces.stage.request.approver;

import com.soda.global.response.ApiResponseForm;
import com.soda.project.application.stage.request.approver.ApproverDesignationFacade;
import com.soda.project.interfaces.stage.request.dto.ApproverDeleteResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ApproverDesignationController {

    private final ApproverDesignationFacade approverDesignationFacade;

    @DeleteMapping("requests/{requestId}/approver/{approverId}")
    public ResponseEntity<ApiResponseForm<?>> deleteApprover(@PathVariable Long approverId,
                                                             HttpServletRequest request) {
        Long memberId = (Long) request.getAttribute("memberId");
        ApproverDeleteResponse approverDeleteResponse = approverDesignationFacade.deleteApprover(approverId, memberId);
        return ResponseEntity.ok(ApiResponseForm.success(approverDeleteResponse));
    }
}
