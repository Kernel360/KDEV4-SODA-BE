package com.soda.member.service;

import com.soda.global.response.ErrorCode;
import com.soda.global.response.GeneralException;
import com.soda.member.dto.company.CompanyRequest;
import com.soda.member.dto.company.CompanyResponse;
import com.soda.member.dto.company.MemberResponse;
import com.soda.member.entity.Company;
import com.soda.member.repository.CompanyRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;

    /**
     * 회사 생성 메서드
     *
     * @param request 회사 생성 요청 DTO
     * @return 생성된 회사 정보 DTO
     * @throws GeneralException 사업자 등록번호 중복 시 발생
     */
    @Transactional
    public CompanyResponse createCompany(CompanyRequest request) {
        // 사업자 등록번호 중복 확인
        if (companyRepository.findByCompanyNumber(request.getCompanyNumber()).isPresent()) {
            throw new GeneralException(ErrorCode.DUPLICATE_COMPANY_NUMBER);
        }

        Company company = Company.builder()
                .name(request.getName())
                .phoneNumber(request.getPhoneNumber())
                .companyNumber(request.getCompanyNumber())
                .address(request.getAddress())
                .detailAddress(request.getDetailAddress())
                .build();

        Company savedCompany = companyRepository.save(company);
        return CompanyResponse.fromEntity(savedCompany);
    }
    /**
     * 모든 회사 조회 메서드
     *
     * @return 모든 회사 정보 DTO 리스트
     */
    public List<CompanyResponse> getAllCompanies() {
        return companyRepository.findByIsDeletedFalse().stream()
                .map(CompanyResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 회사 ID로 조회 메서드
     *
     * @param id 회사 ID
     * @return 회사 정보 DTO
     * @throws GeneralException 회사를 찾을 수 없는 경우 발생
     */
    public CompanyResponse getCompanyById(Long id) {
        Company company = companyRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND_COMPANY));
        return CompanyResponse.fromEntity(company);
    }

    /**
     * 회사 수정 메서드
     *
     * @param id 회사 ID
     * @param request 회사 수정 요청 DTO
     * @return 수정된 회사 정보 DTO
     * @throws GeneralException 회사를 찾을 수 없는 경우 또는 사업자 등록번호 중복 시 발생
     */
    @Transactional
    public CompanyResponse updateCompany(Long id, CompanyRequest request) {
        Company company = companyRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND_COMPANY));

        // 수정하려는 사업자 등록번호가 다른 회사의 사업자 등록번호와 중복되는지 확인
        Optional<Company> existingCompany = companyRepository.findByCompanyNumber(request.getCompanyNumber());
        if (existingCompany.isPresent() && !existingCompany.get().getId().equals(id)) {
            throw new GeneralException(ErrorCode.DUPLICATE_COMPANY_NUMBER);
        }

        company.updateCompany(request);
        Company updatedCompany = companyRepository.save(company);
        return CompanyResponse.fromEntity(updatedCompany);
    }


    /**
     * 회사 삭제 메서드 (소프트 삭제)
     *
     * @param id 회사 ID
     * @throws GeneralException 회사를 찾을 수 없는 경우 발생
     */
    @Transactional
    public void deleteCompany(Long id) {
        Company company = companyRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND_COMPANY));

        company.delete();
        companyRepository.save(company);
    }

    /**
     * 회사 복구 메서드
     *
     * @param id 회사 ID
     * @return 복구된 회사 정보 DTO
     * @throws GeneralException 삭제된 회사를 찾을 수 없는 경우 발생
     */
    @Transactional
    public CompanyResponse restoreCompany(Long id) {
        Company company = companyRepository.findByIdAndIsDeletedTrue(id)
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND_COMPANY));

        company.markAsActive();
        Company restoredCompany = companyRepository.save(company);
        return CompanyResponse.fromEntity(restoredCompany);
    }


}