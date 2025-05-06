package com.soda.member.infrastructure;

import com.soda.member.domain.company.Company;
import com.soda.member.domain.company.CompanyProvider;
import com.soda.member.interfaces.dto.CompanyCreationStatRaw;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CompanyProviderImpl implements CompanyProvider {
    private final CompanyRepository companyRepository;

    @Override
    public Company store(Company company) {
        return companyRepository.save(company);
    }

    @Override
    public Optional<Company> findById(Long id) {
        return companyRepository.findById(id);
    }

    @Override
    public List<Company> findAll() {
        return companyRepository.findAll();
    }

    @Override
    public Optional<Company> findByCompanyNumber(String companyNumber) {
        return companyRepository.findByCompanyNumber(companyNumber);
    }

    @Override
    public List<Company> findByIsDeletedFalse() {
        return companyRepository.findByIsDeletedFalse();
    }

    @Override
    public Optional<Company> findByIdAndIsDeletedFalse(Long id) {
        return companyRepository.findByIdAndIsDeletedFalse(id);
    }

    @Override
    public List<CompanyCreationStatRaw> countCompaniesByDayRaw(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return companyRepository.countCompaniesByDayRaw(startDateTime, endDateTime);
    }
}