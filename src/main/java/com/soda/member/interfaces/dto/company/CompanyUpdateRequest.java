package com.soda.member.interfaces.dto.company;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CompanyUpdateRequest {
    @Size(max = 100, message = "회사명은 100자를 초과할 수 없습니다.")
    private String name;

    @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "올바른 전화번호 형식이 아닙니다.")
    private String phoneNumber;

    @Pattern(regexp = "^\\d{3}-\\d{2}-\\d{5}$", message = "올바른 사업자등록번호 형식이 아닙니다.")
    private String companyNumber;

    @Size(max = 200, message = "주소는 200자를 초과할 수 없습니다.")
    private String address;

    @Size(max = 100, message = "상세주소는 100자를 초과할 수 없습니다.")
    private String detailAddress;

    @Size(max = 50, message = "대표자명은 50자를 초과할 수 없습니다.")
    private String ownerName;
}
