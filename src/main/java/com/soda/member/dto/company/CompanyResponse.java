package com.soda.member.dto.company;

import com.soda.member.entity.Company;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompanyResponse {
    private Long id;
    private String name;
    private String phoneNumber;
    private String companyNumber;
    private String address;
    private String detailAddress;

    public static CompanyResponse fromEntity(Company company) {
        CompanyResponse response = new CompanyResponse();
        response.setId(company.getId());
        response.setName(company.getName());
        response.setPhoneNumber(company.getPhoneNumber());
        response.setCompanyNumber(company.getCompanyNumber());
        response.setAddress(company.getAddress());
        response.setDetailAddress(company.getDetailAddress());
        return response;
    }
}
