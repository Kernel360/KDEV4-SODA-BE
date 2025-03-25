package com.soda.member.controller;

import com.soda.global.response.ApiResponseForm;
import com.soda.member.dto.company.CompanyRequest;
import com.soda.member.dto.company.CompanyResponse;
import com.soda.member.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}