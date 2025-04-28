package com.soda.member.service;


import com.soda.global.log.dataLog.annotation.LoggableEntityAction;
import com.soda.global.response.GeneralException;
import com.soda.member.dto.CompanyCreationStatRaw;
import com.soda.member.dto.CompanyCreationTrend;
import com.soda.member.dto.company.*;
import com.soda.member.entity.Company;
import com.soda.member.enums.StatisticsUnit;
import com.soda.member.error.CompanyErrorCode;
import com.soda.member.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyService {

    private final CompanyRepository companyRepository;

    /**
     * 회사 생성 메서드
     *
     * @param request 회사 생성 요청 DTO
     * @return 생성된 회사 정보 DTO
     * @throws GeneralException 사업자 등록번호 중복 시 발생
     */
    @LoggableEntityAction(action = "CREATE", entityClass = Company.class)
    @Transactional
    public CompanyResponse createCompany(CompanyCreateRequest request) {
        if (companyRepository.findByCompanyNumber(request.getCompanyNumber()).isPresent()) {
            log.error("회사 생성 실패: 사업자 등록번호 중복 - {}", request.getCompanyNumber());
            throw new GeneralException(CompanyErrorCode.DUPLICATE_COMPANY_NUMBER);
        }

        Company company = Company.builder()
                .name(request.getName())
                .phoneNumber(request.getPhoneNumber())
                .companyNumber(request.getCompanyNumber())
                .ownerName(request.getOwnerName())
                .address(request.getAddress())
                .detailAddress(request.getDetailaddress())
                .build();

        Company savedCompany = companyRepository.save(company);
        log.info("회사 생성 성공: {}", savedCompany.getId());
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
        Company company = getCompany(id);
        return CompanyResponse.fromEntity(company);
    }

    /**
     * 회사 수정 메서드
     *
     * @param id      회사 ID
     * @param request 회사 수정 요청 DTO
     * @return 수정된 회사 정보 DTO
     * @throws GeneralException 회사를 찾을 수 없는 경우 또는 사업자 등록번호 중복 시 발생
     */
    @LoggableEntityAction(action = "UPDATE", entityClass = Company.class)
    @Transactional
    public CompanyResponse updateCompany(Long id, CompanyUpdateRequest request) {
        Company company = getCompany(id);

        Optional<Company> existingCompany = companyRepository.findByCompanyNumber(request.getCompanyNumber());
        if (existingCompany.isPresent() && !existingCompany.get().getId().equals(id)) {
            log.error("회사 수정 실패: 사업자 등록번호 중복 - {}", request.getCompanyNumber());
            throw new GeneralException(CompanyErrorCode.DUPLICATE_COMPANY_NUMBER);
        }

        company.updateCompany(request);
        Company updatedCompany = companyRepository.save(company);
        log.info("회사 수정 성공: {}", updatedCompany.getId());
        return CompanyResponse.fromEntity(updatedCompany);
    }

    /**
     * 회사 삭제 메서드 (소프트 삭제)
     *
     * @param id 회사 ID
     * @throws GeneralException 회사를 찾을 수 없는 경우 발생
     */
    @LoggableEntityAction(action = "DELETE", entityClass = Company.class)
    @Transactional
    public void deleteCompany(Long id) {
        Company company = getCompany(id);

        company.delete();
        companyRepository.save(company);
        log.info("회사 삭제 성공: {}", id);
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
        Company company = getCompany(id);

        company.markAsActive();
        Company restoredCompany = companyRepository.save(company);
        log.info("회사 복구 성공: {}", restoredCompany.getId());
        return CompanyResponse.fromEntity(restoredCompany);
    }

    /**
     * 회사 멤버 조회 메서드
     *
     * @param companyId 회사 ID
     * @return 회사 멤버 정보 DTO 리스트
     * @throws GeneralException 회사를 찾을 수 없는 경우 발생
     */
    public List<MemberResponse> getCompanyMembers(Long companyId) {
        Company company = getCompany(companyId);

        return company.getMemberList().stream()
                .map(MemberResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 활성 상태인 회사 엔티티 조회 (ID 기반, 내부 또는 다른 서비스에서 사용)
     *
     * @param companyId 회사 ID
     * @return Company 엔티티
     * @throws GeneralException 회사를 찾을 수 없거나 삭제된 경우 발생
     */
    public Company getCompany(Long companyId) {
        return companyRepository.findByIdAndIsDeletedFalse(companyId)
                .orElseThrow(() -> {
                    log.error("회사 조회 실패: ID {} 에 해당하는 활성 회사를 찾을 수 없음", companyId);
                    return new GeneralException(CompanyErrorCode.NOT_FOUND_COMPANY);
                });
    }

    public List<CompanyCreationTrend> getCompanyCreationTrend(CompanyTrendSearchCondition condition) {

        StatisticsUnit unit = (condition.getUnit() != null) ? condition.getUnit() : StatisticsUnit.MONTH;
        LocalDate endDate = (condition.getEndDate() != null) ? condition.getEndDate() : LocalDate.now();
        LocalDate startDate = (condition.getStartDate() != null) ? condition.getStartDate() : calculateDefaultStartDate(unit, endDate);

        if (startDate.isAfter(endDate)) {
            log.warn("조회 기간 오류: 시작일({})이 종료일({})보다 이후입니다.", startDate, endDate);
            return new ArrayList<>();
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

        List<CompanyCreationStatRaw> rawData = companyRepository.countCompaniesByDayRaw(startDateTime, endDateTime);

        Map<String, Long> aggregatedData = rawData.stream()
                .collect(Collectors.groupingBy(
                        raw -> formatPeriodKey(raw, unit),
                        LinkedHashMap::new,
                        Collectors.summingLong(CompanyCreationStatRaw::getCount)
                ));

        return generateFullPeriodList(startDate, endDate, unit).stream()
                .map(periodStr -> {
                    long count = aggregatedData.getOrDefault(periodStr, 0L);
                    return new CompanyCreationTrend(periodStr, count);
                })
                .collect(Collectors.toList());
    }

    private String formatPeriodKey(CompanyCreationStatRaw raw, StatisticsUnit unit) {
        LocalDate date = LocalDate.of(raw.getYear(), raw.getMonth(), raw.getDayOfMonth());
        switch (unit) {
            case DAY:
                return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            case WEEK:
                return StatisticsUnit.WEEK.formatPeriod(date);
            case MONTH:
            default:
                return YearMonth.from(date).format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }
    }

    private LocalDate calculateDefaultStartDate(StatisticsUnit unit, LocalDate endDate) {
        return switch (unit) {
            case DAY -> endDate.minusDays(6);
            case WEEK -> endDate.minusWeeks(3).with(WeekFields.ISO.dayOfWeek(), 1);
            case MONTH -> endDate.minusMonths(3).withDayOfMonth(1);
        };
    }

    private List<String> generateFullPeriodList(LocalDate startDate, LocalDate endDate, StatisticsUnit unit) {
        List<String> periods = new ArrayList<>();
        LocalDate currentDate = unit.getStartOfPeriod(startDate);
        while (!currentDate.isAfter(endDate)) {
            periods.add(unit.formatPeriod(currentDate));
            currentDate = unit.getNextPeriodStart(currentDate);
        }
        return periods.stream().distinct().sorted().collect(Collectors.toList());
    }
}