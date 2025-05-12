package com.soda.member.interfaces.dto.company;

import com.soda.member.domain.company.Company;
import com.soda.member.domain.company.CompanyProvider;

import java.util.List;
import java.util.function.Function;

public enum CompanyViewOption {
    ACTIVE(provider -> provider.findByIsDeletedFalse()),
    ALL(provider -> provider.findAll()),
    DELETED(provider -> provider.findByIsDeletedTrue());

    // 방법 1: Function<CompanyProvider, List<Company>> 사용
    private final Function<CompanyProvider, List<Company>> retrievalFunction;

    CompanyViewOption(Function<CompanyProvider, List<Company>> retrievalFunction) {
        this.retrievalFunction = retrievalFunction;
    }

    public List<Company> getCompanies(CompanyProvider companyProvider) {
        return this.retrievalFunction.apply(companyProvider);
    }
}
