package com.soda.member.domain;

import com.soda.global.log.data.annotation.LoggableEntityAction;
import com.soda.global.response.GeneralException;
import com.soda.member.interfaces.dto.company.CompanyCreateRequest;
import com.soda.member.interfaces.dto.company.CompanyResponse;
import com.soda.member.interfaces.dto.company.CompanyUpdateRequest;
import com.soda.member.interfaces.dto.company.MemberResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyService {
    private final CompanyProvider companyProvider;

    @LoggableEntityAction(action = "CREATE", entityClass = Company.class)
    @Transactional
    public CompanyResponse createCompany(CompanyCreateRequest request) {
        Company company = Company.create(request);
        return CompanyResponse.fromEntity(companyProvider.store(company));
    }

    public List<CompanyResponse> getAllCompanies() {
        return companyProvider.findByIsDeletedFalse().stream()
                .map(CompanyResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public CompanyResponse getCompanyById(Long id) {
        return companyProvider.findByIdAndIsDeletedFalse(id)
                .map(CompanyResponse::fromEntity)
                .orElseThrow(() -> new GeneralException(CompanyErrorCode.NOT_FOUND_COMPANY));
    }

    public Company getCompany(Long id) {
        return companyProvider.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new GeneralException(CompanyErrorCode.NOT_FOUND_COMPANY));
    }

    @LoggableEntityAction(action = "UPDATE", entityClass = Company.class)
    @Transactional
    public CompanyResponse updateCompany(Long id, CompanyUpdateRequest request) {
        Company company = companyProvider.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new GeneralException(CompanyErrorCode.NOT_FOUND_COMPANY));

        company.update(request);
        return CompanyResponse.fromEntity(companyProvider.store(company));
    }

    @LoggableEntityAction(action = "DELETE", entityClass = Company.class)
    @Transactional
    public void deleteCompany(Long id) {
        Company company = companyProvider.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new GeneralException(CompanyErrorCode.NOT_FOUND_COMPANY));
        company.delete();
        companyProvider.store(company);
    }

    @Transactional
    public CompanyResponse restoreCompany(Long id) {
        Company company = companyProvider.findById(id)
                .orElseThrow(() -> new GeneralException(CompanyErrorCode.NOT_FOUND_COMPANY));
        company.restore();
        return CompanyResponse.fromEntity(companyProvider.store(company));
    }

    public List<MemberResponse> getCompanyMembers(Long companyId) {
        Company company = companyProvider.findByIdAndIsDeletedFalse(companyId)
                .orElseThrow(() -> new GeneralException(CompanyErrorCode.NOT_FOUND_COMPANY));
        return company.getMemberList().stream()
                .map(MemberResponse::fromEntity)
                .collect(Collectors.toList());
    }
}