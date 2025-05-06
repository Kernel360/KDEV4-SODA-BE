package com.soda.member.interfaces.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResetPasswordRequest {
    private String email;
    private String newPassword;
}