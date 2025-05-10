package com.soda.member.interfaces;

import com.soda.global.response.ApiResponseForm;
import com.soda.member.interfaces.dto.CompanyCreationTrend;
import com.soda.member.interfaces.dto.company.*;
import com.soda.member.application.CompanyFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/companies")
public class CompanyController {
    private final CompanyFacade companyFacade;

    @PostMapping
    public ResponseEntity<ApiResponseForm<CompanyResponse>> createCompany(
            @Valid @RequestBody CompanyCreateRequest request) {
        CompanyResponse response = companyFacade.createCompany(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseForm.success(response, "회사 등록 성공"));
    }

    @GetMapping
    public ResponseEntity<ApiResponseForm<List<CompanyResponse>>> getAllCompanies(
            @RequestParam(name = "view", defaultValue = "ACTIVE") CompanyViewOption viewOption) {
        List<CompanyResponse> companies = companyFacade.getAllCompanies(viewOption);
        return ResponseEntity.ok(ApiResponseForm.success(companies, "회사리스트 조회 성공"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseForm<CompanyResponse>> getCompanyById(@PathVariable Long id) {
        CompanyResponse company = companyFacade.getCompanyById(id);
        return ResponseEntity.ok(ApiResponseForm.success(company, "회사 조회 성공"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseForm<CompanyResponse>> updateCompany(
            @PathVariable Long id,
            @Valid @RequestBody CompanyUpdateRequest request) {
        CompanyResponse updatedCompany = companyFacade.updateCompany(id, request);
        return ResponseEntity.ok(ApiResponseForm.success(updatedCompany, "회사 수정 성공"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseForm<Void>> deleteCompany(@PathVariable Long id) {
        companyFacade.deleteCompany(id);
        return ResponseEntity.ok(ApiResponseForm.success(null, "회사 삭제 성공"));
    }

    @PutMapping("/{id}/restore")
    public ResponseEntity<ApiResponseForm<CompanyResponse>> restoreCompany(@PathVariable Long id) {
        CompanyResponse restoredCompany = companyFacade.restoreCompany(id);
        return ResponseEntity.ok(ApiResponseForm.success(restoredCompany, "회사 복구 성공"));
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<ApiResponseForm<List<MemberResponse>>> getCompanyMembers(@PathVariable Long id) {
        List<MemberResponse> members = companyFacade.getCompanyMembers(id);
        return ResponseEntity.ok(ApiResponseForm.success(members, "회사 멤버 정보 조회 성공"));
    }

    @GetMapping("/company-creation-trend")
    public ResponseEntity<ApiResponseForm<List<CompanyCreationTrend>>> getCompanyCreationTrend(
            @Valid @ModelAttribute CompanyTrendSearchCondition searchCondition) {
        List<CompanyCreationTrend> trendData = companyFacade.getCompanyCreationTrend(searchCondition);
        return ResponseEntity.ok(ApiResponseForm.success(trendData, "회사 생성 추이 조회 성공"));
    }
}