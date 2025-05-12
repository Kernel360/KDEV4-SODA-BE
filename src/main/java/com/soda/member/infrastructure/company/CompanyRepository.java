package com.soda.member.infrastructure.company;

import com.soda.member.domain.company.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long>, CompanyRepositoryCustom {
    Optional<Company> findByIdAndIsDeletedFalse(Long companyId);

    List<Company> findByIsDeletedFalse();

    Optional<Company> findByCompanyNumber(String companyNumber);

    List<Company> findByIdInAndIsDeletedFalse(List<Long> companyIds);
}
