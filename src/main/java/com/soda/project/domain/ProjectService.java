package com.soda.project.domain;

import com.querydsl.core.Tuple;
import com.soda.global.log.data.annotation.LoggableEntityAction;
import com.soda.global.response.GeneralException;
import com.soda.member.entity.Company;
import com.soda.member.entity.Member;
import com.soda.member.service.CompanyService;
import com.soda.member.service.MemberService;
import com.soda.project.domain.company.CompanyProjectService;
import com.soda.project.domain.company.CompanyProjectRole;
import com.soda.project.domain.member.MemberProject;
import com.soda.project.domain.member.MemberProjectService;
import com.soda.project.domain.member.MemberProjectRole;
import com.soda.project.infrastructure.ProjectDailyStatsRepository;
import com.soda.project.infrastructure.ProjectRepository;
import com.soda.project.interfaces.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class ProjectService {
    private static final String ADMIN_ROLE = "ADMIN";
    private static final String USER_ROLE = "USER";

    private final ProjectProvider projectProvider;
    private final ProjectRepository projectRepository;
    private final MemberService memberService;
    private final CompanyService companyService;
    private final CompanyProjectService companyProjectService;
    private final MemberProjectService memberProjectService;
    private final ProjectDailyStatsRepository projectDailyStatsRepository;

    /**
     * 프로젝트를 생성하는 메서드
     */
    public Project createAndStoreProject(
            String title, String description, LocalDateTime startDate, LocalDateTime endDate,
            List<Company> clientCompanies, List<Member> clientManagers, List<Member> clientMembers,
            List<String> initialStageNames) {

        log.debug("ProjectService: 프로젝트 생성 및 저장 시작");

        Project project = Project.create(
                title, description, startDate, endDate,
                clientCompanies, clientManagers, clientMembers,
                initialStageNames
        );

        Project savedProject = projectProvider.store(project);
        log.info("ProjectService: 프로젝트 저장 완료: projectId={}", savedProject.getId());
        return savedProject;
    }

    /**
     * 기존 프로젝트에 개발사 및 해당 개발사의 담당자/참여자를 지정하는 메서드
     */
    public void assignDevCompanyAndMembers(Project project, List<Company> devCompanies, List<Member> devManagers, List<Member> devMembers) {
        log.info("[ProjectService] 개발사 및 멤버 할당 시작: projectId={}", project.getId());
        // 1. Project 엔티티 메서드 호출하여 연관관계 설정 (메모리)
        project.assignDevCompanies(devCompanies);
        project.assignDevMembers(devManagers, devMembers);
        // 2. 변경된 Project 엔티티 저장
        projectProvider.store(project);
        log.info("[ProjectService] 개발사 및 멤버 할당 및 저장 완료: projectId={}", project.getId());
    }

    /**
     * 프로젝트 상태 변경
     */
    public void changeProjectStatus(Project project, ProjectStatus newStatus) {
        log.info("프로젝트 상태 변경 시도: ID={}, 현재 상태={}, 변경 상태={}",
                project.getId(), project.getStatus(), newStatus);
        project.changeStatus(newStatus);
        projectProvider.store(project);
        log.info("프로젝트 상태 변경 완료: ID={}, 새 상태={}", project.getId(), project.getStatus());
    }

    /**
     * 프로젝트 기본 정보 수정
     */
    public void updateProjectInfo(Project project, String title, String description, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("프로젝트 정보 수정 시작: projectId={}, title={}", project.getId(), title);
        project.updateProjectInfo(title, description, startDate, endDate);
        projectProvider.store(project);
        log.info("프로젝트 정보 수정 완료: projectId={}", project.getId());
    }

    /**
     * 전체 프로젝트 목록 조회하는 메서드
     */
    public Page<ProjectListResponse> getAllProjects(ProjectSearchCondition condition, Pageable pageable) {
        return projectProvider.searchProjects(condition, pageable);
    }

    /**
     * 특정 사용자가 참여한 프로젝트 목록 조회 메서드
     */
    public Page<Tuple> findMyProjectsData(ProjectSearchCondition condition, Long userId, Pageable pageable) {
        return projectProvider.findMyProjectsData(condition, userId, pageable);
    }

    /**
     * 사용자의 회사가 참여한 프로젝트 목록 조회
     */
    public Page<Tuple> findMyCompanyProjectsData(Long userId, Long companyId, Pageable pageable) {
        return projectProvider.findMyCompanyProjectsData(userId, companyId, pageable);
    }

    /**
     * 프로젝트 삭제
     */
    public void deleteProject(Project project) {
        projectProvider.delete(project);
        log.info("ProjectService: 프로젝트 삭제 완료: projectId={}", project.getId());
    }

    /**
     * 프로젝트에 회사 및 멤버 추가
     */
    public void addCompanyAndMembersToProject(Project project, Company company, CompanyProjectRole companyRole,
                                              List<Member> managers, List<Member> members) {
        log.info("[ProjectService] 회사({}) 및 멤버 추가 시작: projectId={}, role={}",
                company.getName(), project.getId(), companyRole);

        // 1. 역할에 따라 Project 엔티티 메서드 호출하여 연관관계 설정
        if (companyRole == CompanyProjectRole.CLIENT_COMPANY) {
            project.assignClientCompanies(List.of(company));
            project.assignClientMembers(managers, members);
        } else if (companyRole == CompanyProjectRole.DEV_COMPANY) {
            project.assignDevCompanies(List.of(company));
            project.assignDevMembers(managers, members);
        } else {
            log.error("지원하지 않는 회사 역할입니다: {}", companyRole);
        }
        // 2. 변경된 Project 엔티티 저장
        projectProvider.store(project);
        log.info("[ProjectService] 회사({}) 및 멤버 추가 및 저장 완료: projectId={}, role={}",
                company.getName(), project.getId(), companyRole);
    }

    private void validateAdminRole(String userRole) {
        if (!ADMIN_ROLE.equals(userRole)) {
            log.error("권한 없음: 요청한 사용자 역할 = {}", userRole);
            throw new GeneralException(ProjectErrorCode.UNAUTHORIZED_USER);
        }
    }

    public Project getValidProject(Long projectId) {
        return projectProvider.findByIdAndIsDeletedFalse(projectId)
                .orElseThrow(() -> {
                    log.error("프로젝트를 찾을 수 없음: 프로젝트 ID = {}", projectId);
                    return new GeneralException(ProjectErrorCode.PROJECT_NOT_FOUND);
                });
    }

    public ProjectStatsResponse getProjectCreationTrend(Long userId, String userRole, ProjectStatsCondition statsRequest) {
        // ADMIN 유효성 검사
        validateAdminRole(userRole);

        // 날짜 유효성 검사
        LocalDate startDate = statsRequest.getStartDate();
        LocalDate endDate = statsRequest.getEndDate();
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            log.warn("잘못된 조회 기간: StartDate={}, EndDate={}", startDate, endDate);
            throw new GeneralException(ProjectErrorCode.INVALID_DATE_RANGE);
        }

        // 집계 데이터 조회
        List<Tuple> statsData = projectDailyStatsRepository.findProjectCreationStats(startDate, endDate, statsRequest.getTimeUnit());
        log.debug("프로젝트 생성 통계 데이터 조회 완료: {}개의 데이터", statsData.size());

        // 조회 결과를 Map 변환
        Map<String, Long> statsMap = statsData.stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(0, String.class),
                        tuple -> tuple.get(1, Long.class) != null ? tuple.get(1, Long.class) : 0L
                ));
        
        // 조회 기간 내 모든 날짜 생성 및 0으로 채우기
        List<ProjectStatsResponse.DataPoint> trendData = generateFullTrendData(startDate, endDate, statsRequest.getTimeUnit(), statsMap);
        log.debug("누락 기간 0 처리 및 최종 DataPoint 리스트 생성 완료. Size: {}", trendData.size());

        return ProjectStatsResponse.from(statsRequest, trendData);
    }

    private List<ProjectStatsResponse.DataPoint> generateFullTrendData(LocalDate startDate, LocalDate endDate,
                                                                       ProjectStatsCondition.TimeUnit timeUnit, Map<String, Long> statsMap) {
        List<ProjectStatsResponse.DataPoint> fullTrend = new ArrayList<>();

        // 시간 단위에 따라 DataPoint 생성
        switch (timeUnit) {
            case DAY:
                LocalDate currentDate = startDate;
                while (!currentDate.isAfter(endDate)) {
                    String dateKey = currentDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
                    long count = statsMap.getOrDefault(dateKey, 0L);

                    fullTrend.add(ProjectStatsResponse.DataPoint.builder()
                                    .date(dateKey)
                                    .count(count)
                            .build());

                    currentDate = currentDate.plusDays(1);
                }
                break;

            case WEEK:
                WeekFields weekFields = WeekFields.of(Locale.KOREA);
                LocalDate currentWeekStart = startDate.with(weekFields.dayOfWeek(), 1); // 시작일이 속한 주의 시작일 (월요일)
                LocalDate endWeekStart = endDate.with(weekFields.dayOfWeek(), 1); // 종료일이 속한 주의 시작일 (월요일)

                while (!currentWeekStart.isAfter(endWeekStart)) {

                    String repositoryDateKey = currentWeekStart.format(DateTimeFormatter.ofPattern("yyyy-ww", Locale.KOREA));
                    int year = currentWeekStart.getYear();
                    int month = currentWeekStart.getMonthValue();
                    int weekOfMonth = currentWeekStart.get(weekFields.weekOfMonth());
                    String displayDateString = String.format("%d년 %d월 %d주차", year, month, weekOfMonth);
                    long count = statsMap.getOrDefault(repositoryDateKey, 0L);

                    fullTrend.add(ProjectStatsResponse.DataPoint.builder()
                            .date(displayDateString)
                            .count(count)
                            .build());
                    currentWeekStart = currentWeekStart.plusWeeks(1);
                }
                break;

            case MONTH:
                YearMonth currentMonth = YearMonth.from(startDate);
                YearMonth endMonth = YearMonth.from(endDate);
                DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");

                while (!currentMonth.isAfter(endMonth)) {
                    String dateKey = currentMonth.format(monthFormatter);
                    long count = statsMap.getOrDefault(dateKey, 0L);

                    fullTrend.add(ProjectStatsResponse.DataPoint.builder()
                            .date(dateKey)
                            .count(count)
                            .build());
                    currentMonth = currentMonth.plusMonths(1);
                }
                break;
        }

        return fullTrend;
    }

}
