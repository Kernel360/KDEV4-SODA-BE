package com.soda.project.interfaces.dto;

import com.soda.project.domain.company.enums.CompanyProjectRole;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;

@Getter
public class ProjectCompanyAddRequest {

    @NotNull(message = "회사 ID는 필수입니다.")
    private Long companyId;

    @NotNull(message = "회사 역할은 필수입니다. (고객사/개발사")
    private CompanyProjectRole role;

    @NotNull(message = "담당자 선택은 필수입니다.")
    private List<Long> managerIds;

    private List<Long> memberIds;
}
