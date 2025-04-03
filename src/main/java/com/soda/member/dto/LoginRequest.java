package com.soda.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginRequest {
    @Schema(description = "사용자 ID", example = "admin01")
    private String authId;

    @Schema(description = "비밀번호", example = "admin01!")
    private String password;

}
