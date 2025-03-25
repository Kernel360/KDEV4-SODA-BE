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


}