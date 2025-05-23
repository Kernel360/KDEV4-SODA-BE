package com.soda.member.application;

import com.soda.global.log.data.annotation.LoggableEntityAction;
import com.soda.member.domain.company.Company;
import com.soda.member.domain.company.CompanyService;
import com.soda.member.domain.company.CompanyStatsService;
import com.soda.member.interfaces.dto.CompanyCreationTrend;
import com.soda.member.interfaces.dto.company.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyFacade {
    private final CompanyService companyService;
    private final CompanyStatsService companyStatsService;

    @Transactional
    @LoggableEntityAction(action = "CREATE", entityClass = Company.class)
    public CompanyResponse createCompany(CompanyCreateRequest request) {
        return companyService.createCompany(request);
    }

    public List<CompanyResponse> getAllCompanies() {
        return companyService.getAllCompanies();
    }

    public CompanyResponse getCompanyById(Long id) {
        return companyService.getCompanyById(id);
    }

    public Company getCompany(Long id) {
        return companyService.getCompany(id);
    }

    @Transactional
    @LoggableEntityAction(action = "UPDATE", entityClass = Company.class)
    public CompanyResponse updateCompany(Long id, CompanyUpdateRequest request) {
        return companyService.updateCompany(id, request);
    }

    @Transactional
    @LoggableEntityAction(action = "DELETE", entityClass = Company.class)
    public void deleteCompany(Long id) {
        companyService.deleteCompany(id);
    }

    @Transactional
    public CompanyResponse restoreCompany(Long id) {
        return companyService.restoreCompany(id);
    }

    public List<MemberResponse> getCompanyMembers(Long companyId) {
        return companyService.getCompanyMembers(companyId);
    }

    public List<CompanyCreationTrend> getCompanyCreationTrend(CompanyTrendSearchCondition condition) {
        return companyStatsService.getCompanyCreationTrend(condition);
    }
}