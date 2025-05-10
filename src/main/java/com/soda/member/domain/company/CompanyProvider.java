package com.soda.member.domain.company;

import com.soda.member.interfaces.dto.CompanyCreationStatRaw;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CompanyProvider {
        Company store(Company company);

        Optional<Company> findById(Long id);

        List<Company> findAll();

        Optional<Company> findByCompanyNumber(String companyNumber);

        List<Company> findByIsDeletedFalse();

        Optional<Company> findByIdAndIsDeletedFalse(Long id);

        List<CompanyCreationStatRaw> countCompaniesByDayRaw(LocalDateTime startDateTime, LocalDateTime endDateTime);

        List<Company> findByIdInAndIsDeletedFalse(List<Long> companyIds);

        List<Company> findByIsDeletedTrue();
}