package com.soda.member.controller;

import com.soda.global.response.ApiResponseForm;
import com.soda.member.dto.company.CompanyRequest;
import com.soda.member.dto.company.CompanyResponse;
import com.soda.member.dto.company.MemberResponse;
import com.soda.member.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/companies") // 기본 경로 설정
public class CompanyController {
    private final CompanyService companyService;

    // 회사 등록
    @PostMapping
    public ResponseEntity<ApiResponseForm<CompanyResponse>> createCompany(@RequestBody CompanyRequest request) {
        CompanyResponse response = companyService.createCompany(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseForm.success(response, "회사 등록 성공"));
    }

    // 모든 회사 조회
    @GetMapping
    public ResponseEntity<ApiResponseForm<List<CompanyResponse>>> getAllCompanies() {
        List<CompanyResponse> companies = companyService.getAllCompanies();
        return ResponseEntity.ok(ApiResponseForm.success(companies, "회사리스트 조회 성공"));
    }

    // 회사 조회 (ID로)
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseForm<CompanyResponse>> getCompanyById(@PathVariable Long id) {
        CompanyResponse company = companyService.getCompanyById(id);
        return ResponseEntity.ok(ApiResponseForm.success(company, "회사 조회 성공"));
    }

    // 회사 수정
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseForm<CompanyResponse>> updateCompany(@PathVariable Long id, @RequestBody CompanyRequest request) {
        CompanyResponse updatedCompany = companyService.updateCompany(id, request);
        return ResponseEntity.ok(ApiResponseForm.success(updatedCompany, "회사 수정 성공"));
    }

    // 회사 삭제 (소프트 삭제)
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseForm<Void>> deleteCompany(@PathVariable Long id) {
        companyService.deleteCompany(id);
        return ResponseEntity.ok(ApiResponseForm.success(null, "회사 삭제 성공"));
    }

    // 회사 복구
    @PutMapping("/{id}/restore")
    public ResponseEntity<ApiResponseForm<CompanyResponse>> restoreCompany(@PathVariable Long id) {
        CompanyResponse restoredCompany = companyService.restoreCompany(id);
        return ResponseEntity.ok(ApiResponseForm.success(restoredCompany, "회사 복구 성공"));
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<ApiResponseForm<List<MemberResponse>>> getCompanyMembers(@PathVariable Long id) {
        List<MemberResponse> members = companyService.getCompanyMembers(id);
        return ResponseEntity.ok(ApiResponseForm.success(members, "회사 멤버 정보 조회 성공"));
    }
}