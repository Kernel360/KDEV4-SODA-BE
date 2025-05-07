package com.soda.member.interfaces.dto.company;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CompanyUpdateRequest {
    private String name;
    private String phoneNumber;
    private String ownerName;
    private String companyNumber;
    private String address;
    private String detailAddress;
}
