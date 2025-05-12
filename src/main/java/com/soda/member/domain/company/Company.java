package com.soda.member.domain.company;

import com.soda.common.BaseEntity;
import com.soda.member.domain.member.Member;
import com.soda.member.interfaces.dto.company.CompanyCreateRequest;
import com.soda.member.interfaces.dto.company.CompanyUpdateRequest;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    @Builder
    public Company(String name, String phoneNumber, String ownerName,
            String companyNumber, String address, String detailAddress) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.ownerName = ownerName;
        this.companyNumber = companyNumber;
        this.address = address;
        this.detailAddress = detailAddress;
    }

    public static Company create(CompanyCreateRequest request) {
        return Company.builder()
                .name(request.getName())
                .phoneNumber(request.getPhoneNumber())
                .ownerName(request.getOwnerName())
                .companyNumber(request.getCompanyNumber())
                .address(request.getAddress())
                .detailAddress(request.getDetailAddress())
                .build();
    }

    public void update(CompanyUpdateRequest request) {
        if (request.getName() != null)
            this.name = request.getName();
        if (request.getPhoneNumber() != null)
            this.phoneNumber = request.getPhoneNumber();
        if (request.getOwnerName() != null)
            this.ownerName = request.getOwnerName();
        if (request.getCompanyNumber() != null)
            this.companyNumber = request.getCompanyNumber();
        if (request.getAddress() != null)
            this.address = request.getAddress();
        if (request.getDetailAddress() != null)
            this.detailAddress = request.getDetailAddress();
    }

    public void delete() {
        this.markAsDeleted();
    }

    public void restore() {
        this.markAsActive();
    }

    public void addMember(Member member) {
        this.memberList.add(member);
    }

    public void removeMember(Member member) {
        this.memberList.remove(member);
    }
}
