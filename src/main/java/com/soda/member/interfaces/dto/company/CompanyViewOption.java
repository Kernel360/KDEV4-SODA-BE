package com.soda.member.interfaces.dto.company;

public enum CompanyViewOption {
    ACTIVE, // 삭제 안된 회사 (기본값)
    ALL,    // 모든 회사 (삭제된 회사 포함)
    DELETED // 삭제된 회사만
}
