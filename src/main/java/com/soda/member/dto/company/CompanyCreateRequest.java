package com.soda.member.dto.company;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CompanyCreateRequest {
    private String name;
    private String phoneNumber;
    private String companyNumber;
    private String address;
    private String detailaddress;
    private String ownerName;
}
