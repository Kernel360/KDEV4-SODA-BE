package com.soda.member.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VerificationConfirmResponse {
    private boolean isVerified;
    private String email;
}
