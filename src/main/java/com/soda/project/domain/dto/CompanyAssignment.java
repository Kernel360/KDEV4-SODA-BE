package com.soda.project.domain.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CompanyAssignment {

    @NotNull(message = "회사 ID는 필수입니다.")
    private Long companyId;

    // 담당자 목록은 필수
    @NotEmpty(message = "담당자 ID 목록은 필수입니다.")
    private List<Long> managerIds;

    // 일반 참여자 목록은 선택 사항
    private List<Long> memberIds;

}
