package com.soda.member.interfaces.dto;

import com.soda.member.domain.member.MemberRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdminUpdateUserRequestDto {

    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    @Email(message = "유효한 이메일 형식이 아닙니다.")
    @NotBlank(message = "이메일은 필수입니다.")
    private String email;

    @NotNull(message = "역할은 필수입니다.")
    private MemberRole role;

    private Long companyId;

    private String position;

    private String phoneNumber;

}
