package com.soda.member.domain.company;

import com.soda.member.interfaces.dto.CompanyCreationStatRaw;
import com.soda.member.interfaces.dto.company.CompanyViewOption;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CompanyProvider {
        Company store(Company company);

        Optional<Company> findById(Long id);

        Optional<Company> findByIdAndIsDeletedFalse(Long id);

        List<CompanyCreationStatRaw> countCompaniesByDayRaw(LocalDateTime startDateTime, LocalDateTime endDateTime);

        List<Company> findByIdInAndIsDeletedFalse(List<Long> companyIds);

        Page<Company> findAllCompaniesWithSearch(CompanyViewOption viewOption, String searchKeyword, Pageable pageable);
}