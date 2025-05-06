package com.soda.member.interfaces.dto.member.admin;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class UpdateUserStatusRequestDto {
    @NotNull(message = "활성 상태는 필수입니다.")
    private Boolean active;
}