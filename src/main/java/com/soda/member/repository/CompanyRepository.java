package com.soda.member.repository;

import com.soda.member.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByIdAndIsDeletedFalse(Long companyId);

    Optional<Company> findByName(String companyName);

    // 삭제되지 않은 회사 목록을 조회
    List<Company> findByIsDeletedFalse();

    Optional<Company> findByCompanyNumber(String companyNumber);

    Optional<Company> findByIdAndIsDeletedTrue(Long id);
}
