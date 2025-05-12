package com.soda.member.interfaces.dto.company;

import com.soda.member.domain.company.Company;
import com.soda.member.domain.company.CompanyProvider;

import java.util.List;
import java.util.function.Function;

public enum CompanyViewOption {
    ACTIVE(CompanyProvider::findByIsDeletedFalseOrderByCreatedAtDesc),
    ALL(CompanyProvider::findAllByOrderByCreatedAtDesc),
    DELETED(CompanyProvider::findByIsDeletedTrueOrderByCreatedAtDesc);

    private final Function<CompanyProvider, List<Company>> retrievalFunction;

    CompanyViewOption(Function<CompanyProvider, List<Company>> retrievalFunction) {
        this.retrievalFunction = retrievalFunction;
    }

    public List<Company> getCompanies(CompanyProvider companyProvider) {
        return this.retrievalFunction.apply(companyProvider);
    }
}
