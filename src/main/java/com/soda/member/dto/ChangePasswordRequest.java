package com.soda.member.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChangePasswordRequest {
    private String email;
    private String newPassword;
}