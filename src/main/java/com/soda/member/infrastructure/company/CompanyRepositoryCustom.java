package com.soda.member.infrastructure.company;

import com.soda.member.interfaces.dto.CompanyCreationStatRaw;

import java.time.LocalDateTime;
import java.util.List;

public interface CompanyRepositoryCustom {
    List<CompanyCreationStatRaw> countCompaniesByDayRaw(LocalDateTime startDate, LocalDateTime endDate);
}
