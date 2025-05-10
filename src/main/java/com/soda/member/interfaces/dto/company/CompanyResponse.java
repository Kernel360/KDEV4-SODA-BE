package com.soda.member.interfaces.dto.company;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.soda.member.domain.company.Company;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CompanyResponse {
    private Long id;
    private String name;
    private String phoneNumber;
    private String companyNumber;
    private String address;
    private String detailAddress;
    private String ownerName;
    private Boolean isDeleted;

    public static CompanyResponse fromEntity(Company company) {
        if (company == null) {
            return null;
        }
        return CompanyResponse.builder()
                .id(company.getId())
                .name(company.getName())
                .phoneNumber(company.getPhoneNumber())
                .companyNumber(company.getCompanyNumber())
                .address(company.getAddress())
                .detailAddress(company.getDetailAddress())
                .ownerName(company.getOwnerName())
                .isDeleted(company.getIsDeleted())
                .build();
    }
}
