package com.soda.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;

@Getter
@Entity
public class Company extends BaseEntity{

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String companyNumber;

    @Column(nullable = false)
    private String address;

    private String detailAddress;

}
