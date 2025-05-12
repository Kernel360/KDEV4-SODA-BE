package com.soda.member.infrastructure.company;

import com.soda.member.domain.company.Company;
import com.soda.member.interfaces.dto.CompanyCreationStatRaw;
import com.soda.member.interfaces.dto.company.CompanyViewOption;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface CompanyRepositoryCustom {
    List<CompanyCreationStatRaw> countCompaniesByDayRaw(LocalDateTime startDate, LocalDateTime endDate);

    Page<Company> findAllCompaniesWithSearch(CompanyViewOption viewOption, String searchKeyword, Pageable pageable);
}
