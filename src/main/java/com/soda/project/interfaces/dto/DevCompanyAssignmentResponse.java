package com.soda.project.interfaces.dto;

import com.soda.member.domain.company.Company;
import com.soda.member.domain.Member;
import com.soda.project.domain.member.enums.MemberProjectRole;
import lombok.Builder;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

@Getter
@Builder
public class DevCompanyAssignmentResponse {

    // 개발사별 할당 정보 리스트 (CompanyAssignment 직접 사용)
    private List<CompanyAssignment> devAssignments;

    public static DevCompanyAssignmentResponse from(List<CompanyAssignment> assignments) {
        return DevCompanyAssignmentResponse.builder()
                .devAssignments(assignments != null ? assignments : Collections.emptyList())
                .build();
    }

}