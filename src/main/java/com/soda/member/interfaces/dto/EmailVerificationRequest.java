package com.soda.member.interfaces.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmailVerificationRequest {
    private String email;
    private String code;
}
