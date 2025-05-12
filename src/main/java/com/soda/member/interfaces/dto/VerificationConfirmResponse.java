package com.soda.member.interfaces.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VerificationConfirmResponse {
    private boolean isVerified;
    private String email;
}
