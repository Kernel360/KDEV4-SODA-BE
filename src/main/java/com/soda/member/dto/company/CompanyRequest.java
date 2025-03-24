package com.soda.member.dto.company;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompanyRequest {
    private String name;
    private String phoneNumber;
    private String companyNumber;
    private String address;
    private String detailAddress;
}
