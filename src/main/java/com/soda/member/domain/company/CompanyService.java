package com.soda.member.domain.company;

import com.soda.global.response.GeneralException;
import com.soda.member.interfaces.dto.company.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyService {
    private final CompanyProvider companyProvider;

    public CompanyResponse createCompany(CompanyCreateRequest request) {
        Company company = Company.create(request);
        return CompanyResponse.fromEntity(companyProvider.store(company));
    }

    public List<CompanyResponse> getAllCompanies(CompanyViewOption viewOption) {
        List<Company> companies;
        switch (viewOption) {
            case ALL:
                log.debug("모든 회사 조회 (삭제 포함)");
                companies = companyProvider.findAll();
                break;
            case DELETED:
                log.debug("삭제된 회사만 조회");
                companies = companyProvider.findByIsDeletedTrue();
                break;
            case ACTIVE:
            default:
                log.debug("활성 회사만 조회 (삭제 안된 회사)");
                companies = companyProvider.findByIsDeletedFalse();
                break;
        }
        return companies.stream()
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

    public CompanyResponse updateCompany(Long id, CompanyUpdateRequest request) {
        Company company = companyProvider.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new GeneralException(CompanyErrorCode.NOT_FOUND_COMPANY));

        company.update(request);
        return CompanyResponse.fromEntity(companyProvider.store(company));
    }

    public void deleteCompany(Long id) {
        Company company = companyProvider.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new GeneralException(CompanyErrorCode.NOT_FOUND_COMPANY));
        company.delete();
        companyProvider.store(company);
    }

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

    public List<Company> findCompaniesByIds(List<Long> companyIds) {
        if (CollectionUtils.isEmpty(companyIds)) {
            return Collections.emptyList();
        }
        log.debug("ID 목록 {} 로 회사 조회 시작", companyIds);
        List<Company> companies = companyProvider.findByIdInAndIsDeletedFalse(companyIds);
        log.debug("ID 목록 {} 로 활성 회사 {}개 조회 완료", companyIds, companies.size());
        return companies;
    }
}