package com.soda.member.interfaces.dto.member;

import com.soda.member.domain.member.MemberStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class MemberStatusUpdate {
    @NotNull(message = "새로운 멤버 상태는 필수입니다.")
    private MemberStatus newStatus;
}
