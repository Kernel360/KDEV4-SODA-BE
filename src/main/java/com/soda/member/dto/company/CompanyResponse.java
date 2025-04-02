package com.soda.member.dto.company;

import com.soda.member.Company;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CompanyResponse {
    private Long id;
    private String name;
    private String phoneNumber;
    private String companyNumber;
    private String address;
    private String detailAddress;

    public static CompanyResponse fromEntity(Company company) {
        return CompanyResponse.builder()
                .id(company.getId())
                .name(company.getName())
                .phoneNumber(company.getPhoneNumber())
                .companyNumber(company.getCompanyNumber())
                .address(company.getAddress())
                .detailAddress(company.getDetailAddress())
                .build();
    }
}
