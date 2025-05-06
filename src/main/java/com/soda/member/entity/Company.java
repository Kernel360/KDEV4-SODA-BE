package com.soda.member.entity;

import com.soda.common.BaseEntity;
import com.soda.member.interfaces.dto.company.CompanyUpdateRequest;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Company extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String ownerName;

    @Column(nullable = false)
    private String companyNumber;

    @Column(nullable = false)
    private String address;

    private String detailAddress;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL)
    private List<Member> memberList = new ArrayList<>();

    // Entity 수정 메서드
    public void updateCompany(CompanyUpdateRequest request) {
        if (request.getName() != null) {
            this.name = request.getName();
        }
        if (request.getOwnerName() != null) {
            this.ownerName = request.getOwnerName();
        }
        if (request.getPhoneNumber() != null) {
            this.phoneNumber = request.getPhoneNumber();
        }
        if(request.getCompanyNumber() != null){
            this.companyNumber = request.getCompanyNumber();
        }
        if (request.getAddress() != null) {
            this.address = request.getAddress();
        }
        if (request.getDetailAddress() != null) {
            this.detailAddress = request.getDetailAddress();
        }
    }
    public void delete() {
        this.markAsDeleted();
    }
}
