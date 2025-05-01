package com.soda.project.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;

@Getter
public class ProjectMemberAddRequest {

    @NotNull(message = "멤버를 추가할 회사 ID는 필수입니다.")
    private Long companyId;

    // 둘 중 하나만 추가 가능
    private List<Long> managerIds;
    private List<Long> memberIds;

}
