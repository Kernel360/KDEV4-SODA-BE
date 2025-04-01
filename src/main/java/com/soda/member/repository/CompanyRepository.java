package com.soda.member.repository;

import com.soda.member.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByIdAndIsDeletedFalse(Long companyId);

    List<Company> findByIsDeletedFalse();

    Optional<Company> findByCompanyNumber(String companyNumber);

}
