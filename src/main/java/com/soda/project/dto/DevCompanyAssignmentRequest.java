package com.soda.project.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DevCompanyAssignmentRequest {

    @NotNull(message = "개발사 선택은 필수입니다.")
    private List<Long> devCompanyIds;

    @NotNull(message = "개발사 담당자 선택은 필수입니다.")
    private List<Long> devMangerIds;

    private List<Long> devMemberIds;

}
